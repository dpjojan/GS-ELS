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

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private MutualFundService mutualFundService;

    //  All funds your backend supports
    private static final List<MutualFund> ALL_FUNDS = List.of(
    // Low Risk
    new MutualFund("FZILX", "Fidelity ZERO International Index Fund", "Low"),
    new MutualFund("PRDGX", "T. Rowe Price Dividend Growth Fund", "Low"),
    new MutualFund("FSUTX", "Fidelity Select Utilities Portfolio", "Low"),
    // Medium Risk
    new MutualFund("VFIAX", "Vanguard 500 Index Fund Admiral", "Medium"),
    new MutualFund("FXAIX", "Fidelity 500 Index Fund", "Medium"),
    new MutualFund("DODGX", "Dodge & Cox Stock Fund", "Medium"),
    new MutualFund("VDIGX", "Vanguard Dividend Growth Fund", "Medium"),
    new MutualFund("AWSHX", "American Funds Washington Mutual", "Medium"),
    new MutualFund("PRBLX", "Parnassus Core Equity Fund", "Medium"),
    new MutualFund("JENSX", "Jensen Quality Growth Fund", "Medium"),
    new MutualFund("GABAX", "Gabelli Asset Fund", "Medium"),
    new MutualFund("CSIEX", "Calvert Equity Fund", "Medium"),
    new MutualFund("SWPPX", "Schwab S&P 500 Index Fund", "Medium"),
    new MutualFund("VTSAX", "Vanguard Total Stock Market Index Admiral", "Medium"),
    new MutualFund("FSKAX", "Fidelity Total Market Index Fund", "Medium"),
    new MutualFund("SWTSX", "Schwab Total Stock Market Index", "Medium"),
    new MutualFund("VIMAX", "Vanguard Mid-Cap Index Admiral", "Medium"),
    new MutualFund("VTIAX", "Vanguard Total International Stock Index", "Medium"),
    new MutualFund("DODFX", "Dodge & Cox International Stock Fund", "Medium"),
    new MutualFund("VTMGX", "Vanguard Developed Markets Index Admiral", "Medium"),
    new MutualFund("VGHAX", "Vanguard Health Care Fund Admiral", "Medium"),
    new MutualFund("VGSLX", "Vanguard Real Estate Index Admiral", "Medium"),
    new MutualFund("AREEX", "American Century Real Estate Fund", "Medium"),
    // High Risk
    new MutualFund("VSMAX", "Vanguard Small-Cap Index Admiral", "High"),
    new MutualFund("SWLGX", "Schwab Large Cap Growth Fund", "High"),
    new MutualFund("TRBCX", "T. Rowe Price Blue Chip Growth", "High"),
    new MutualFund("AGTHX", "American Funds Growth Fund of America", "High"),
    new MutualFund("FCNTX", "Fidelity Contrafund", "High"),
    new MutualFund("FDGRX", "Fidelity Growth Company Fund", "High"),
    new MutualFund("PRGFX", "T. Rowe Price Growth Stock Fund", "High"),
    new MutualFund("VIGAX", "Vanguard Growth Index Admiral", "High"),
    new MutualFund("FMCSX", "Fidelity Mid-Cap Stock Fund", "High"),
    new MutualFund("RPMGX", "T. Rowe Price Mid-Cap Growth Fund", "High"),
    new MutualFund("RYTRX", "Royce Total Return Fund", "High"),
    new MutualFund("BIASX", "Brown Advisory Small-Cap Growth", "High"),
    new MutualFund("AEPGX", "American Funds EuroPacific Growth", "High"),
    new MutualFund("FIGFX", "Fidelity International Growth Fund", "High"),
    new MutualFund("PRITX", "T. Rowe Price International Stock Fund", "High"),
    new MutualFund("OAKIX", "Oakmark International Fund", "High"),
    new MutualFund("MAPIX", "Matthews Asia Dividend Fund", "High"),
    new MutualFund("BEXFX", "Baron Emerging Markets Fund", "High"),
    new MutualFund("FEMKX", "Fidelity Emerging Markets Fund", "High"),
    new MutualFund("FSPTX", "Fidelity Select Technology Portfolio", "High"),
    new MutualFund("FSPHX", "Fidelity Select Health Care Portfolio", "High"),
    new MutualFund("PRHSX", "T. Rowe Price Health Sciences Fund", "High"),
    new MutualFund("VGELX", "Vanguard Energy Fund Admiral", "High"),
    new MutualFund("FIDSX", "Fidelity Select Financial Services", "High"),
    new MutualFund("BREIX", "Baron Real Estate Fund", "High")
);

    // Build the static fund catalogue context (sent with every request) 
    private String buildFundCatalogueContext() {
    StringBuilder sb = new StringBuilder();
    sb.append("AVAILABLE FUNDS IN THIS APPLICATION:\n");
    sb.append("(Ticker | Fund Name | Risk Level)\n");
    for (MutualFund f : ALL_FUNDS) {
        sb.append(f.getTicker()).append(" | ")
          .append(f.getName()).append(" | ")
          .append(f.getRisk()).append("\n");
    }
    return sb.toString();
}

    // Detect any fund tickers mentioned in the user's message
    private List<MutualFund> detectMentionedFunds(String userMessage) {
        String upper = userMessage.toUpperCase();
        return ALL_FUNDS.stream()
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

        // 4. Call Gemini
        try {
            String body = """
                {"contents":[{"parts":[{"text":"%s"}]}]}
                """.formatted(fullPrompt.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                    "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey
                ))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            String output = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();

            log.info("Gemini responded successfully");
            return output;

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