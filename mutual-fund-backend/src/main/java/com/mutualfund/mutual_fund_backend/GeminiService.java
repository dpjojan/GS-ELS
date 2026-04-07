package com.mutualfund.mutual_fund_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private MutualFundService mutualFundService;

    @Autowired
    private MutualFundRepository mutualFundRepository;

    // Build the fund catalogue context (sent with every request)
    private String buildFundCatalogueContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("AVAILABLE FUNDS IN THIS APPLICATION:\n");
        sb.append("(Ticker | Fund Name | Risk Level)\n");
        for (MutualFund f : mutualFundRepository.findAll()) {
            sb.append(f.getTicker()).append(" | ")
              .append(f.getName()).append(" | ")
              .append(f.getRisk()).append("\n");
        }
        return sb.toString();
    }

    // Detect any fund tickers mentioned in the user's message
    private List<MutualFund> detectMentionedFunds(String userMessage) {
        String upper = userMessage.toUpperCase();
        return mutualFundRepository.findAll().stream()
            .filter(f ->
                upper.contains(f.getTicker()) ||
                upper.contains(f.getName().toUpperCase())
            )
            .toList();
    }

    // Fetch live beta for each detected fund and format it
    private String buildBetaContext(List<MutualFund> funds) {
        if (funds.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\nLIVE BETA DATA (vs S&P 500, 12-month, monthly intervals):\n");
        for (MutualFund f : funds) {
            Optional<Double> beta = mutualFundService.getBetaFromNewtonApi(f.getTicker());
            if (beta.isPresent()) {
                sb.append(String.format("  %s (%s): beta = %.4f%n",
                    f.getName(), f.getTicker(), beta.get()));
            } else {
                sb.append(String.format("  %s (%s): beta = unavailable%n",
                    f.getName(), f.getTicker()));
            }
        }
        return sb.toString();
    }

    // ── System prompt that defines Gemini's role 
    private static final String SYSTEM_PROMPT = """
        You are a mutual fund assistant embedded in a financial application.
        Your job is to help users understand mutual funds, assess risk, and get recommendations.

        GUIDELINES:
        - Only recommend funds that appear in the AVAILABLE FUNDS list provided.
        - Use the live beta data when it is provided to explain risk. Beta > 1 means more volatile \
than the market, beta < 1 means less volatile.
        - When recommending funds, consider the user's apparent risk tolerance from their message.
        - Be concise and use plain English. Avoid jargon unless the user seems experienced.
        - If beta data is unavailable for a fund, say so honestly rather than guessing.
        - Do not make up tickers or fund names not in the list.
        """;

    // ── Main method called by the controller
    public String chat(String userMessage) {
        log.info("GeminiService.chat() called with: \"{}\"", userMessage);

        // 1. Detect funds mentioned in the user's message
        List<MutualFund> mentionedFunds = detectMentionedFunds(userMessage);
        log.info("Detected {} fund(s) in user message", mentionedFunds.size());

        // 2. Build context blocks
        String catalogueContext = buildFundCatalogueContext();
        String betaContext = buildBetaContext(mentionedFunds); // empty if no funds detected

        // 3. Assemble the full prompt
        String fullPrompt = SYSTEM_PROMPT + "\n\n"
            + catalogueContext + "\n"
            + betaContext + "\n"
            + "USER QUESTION: " + userMessage;

        log.debug("Full prompt sent to Gemini:\n{}", fullPrompt);

        // 4. Call Gemini with tool support
        try {
            ObjectMapper mapper = new ObjectMapper();
            HttpClient client = HttpClient.newHttpClient();

            // Tool definitions for Gemini
            String toolsJson = """
                [{"functionDeclarations":[
                  {"name":"get_expected_return",
                   "description":"Gets the historical expected annual return rate for a mutual fund ticker.",
                   "parameters":{"type":"object","properties":{"ticker":{"type":"string","description":"Fund ticker symbol"}},"required":["ticker"]}},
                  {"name":"get_future_value",
                   "description":"Projects the future value of an investment year by year.",
                   "parameters":{"type":"object","properties":{"ticker":{"type":"string"},"principal":{"type":"number","description":"Initial investment in dollars"},"years":{"type":"integer","description":"Number of years"}},"required":["ticker","principal","years"]}}
                ]}]""";

            // Build the initial contents array
            ArrayNode contents = (ArrayNode) mapper.readTree(
                "[{\"role\":\"user\",\"parts\":[{\"text\":\"\"}]}]");
            ((ObjectNode) contents.get(0).path("parts").get(0)).put("text", fullPrompt);

            // Loop: Gemini may call tools before giving a final text answer
            for (int turn = 0; turn < 5; turn++) {
                String body = String.format("{\"contents\":%s,\"tools\":%s}",
                    mapper.writeValueAsString(contents), toolsJson);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                JsonNode root = mapper.readTree(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
                JsonNode content = root.path("candidates").get(0).path("content");
                JsonNode parts = content.path("parts");

                // If Gemini called a tool, execute it and send the result back
                if (parts.get(0).has("functionCall")) {
                    contents.add(content); // append model turn
                    ArrayNode toolResults = mapper.createArrayNode();
                    for (JsonNode part : parts) {
                        String fn   = part.path("functionCall").path("name").asText();
                        JsonNode args = part.path("functionCall").path("args");
                        log.info("Gemini called tool: {} args={}", fn, args);

                        String result;
                        if ("get_expected_return".equals(fn)) {
                            double rate = mutualFundService.getExpectedReturn(args.path("ticker").asText());
                            result = String.format("%.4f (%.2f%%)", rate, rate * 100);
                        } else if ("get_future_value".equals(fn)) {
                            List<Double> vals = mutualFundService.calculateFutureValAllYears(
                                args.path("ticker").asText(), args.path("principal").asDouble(), args.path("years").asInt());
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < vals.size(); i++) sb.append(String.format("Year %d: $%.2f\n", i + 1, vals.get(i)));
                            result = sb.toString();
                        } else { result = "Unknown tool: " + fn; }

                        ObjectNode fr = mapper.createObjectNode();
                        fr.put("name", fn);
                        fr.set("response", mapper.createObjectNode().put("result", result));
                        toolResults.add(mapper.createObjectNode().set("functionResponse", fr));
                    }
                    ObjectNode toolTurn = mapper.createObjectNode();
                    toolTurn.put("role", "user");
                    toolTurn.set("parts", toolResults);
                    contents.add(toolTurn);
                } else {
                    log.info("Gemini responded successfully");
                    return parts.get(0).path("text").asText();
                }
            }
            return "Unable to complete the request.";

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return "Error calling Gemini: " + e.getMessage();
        }
    }

    // ── Keep testGemini for the /gemini-test endpoint ─────────────────────────
    public String testGemini(String prompt) {
        return chat(prompt);
    }
}