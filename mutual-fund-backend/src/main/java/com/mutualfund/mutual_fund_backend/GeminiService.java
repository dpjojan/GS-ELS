package com.mutualfund.mutual_fund_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service // Marks this as a service component (Spring will manage it)
public class GeminiService {

    // Injects your API key from application.properties
    @Value("${gemini.api.key}")
    private String apiKey;

    public String testGemini(String prompt) {
        try {
            // Build the request body in the format Gemini expects
            // "contents" → main wrapper
            // "parts" → actual input text
            String body = """
            {
              "contents":[{"parts":[{"text":"%s"}]}]
            }
            """.formatted(prompt);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    
                    // IMPORTANT: This is the endpoint + model you're calling
                    // Make sure the model exists in your /models API list
                    .uri(URI.create(
                        "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey
                    ))
                    
                    // Tell the API we're sending JSON
                    .header("Content-Type", "application/json")
                    
                    // Attach the request body (your prompt)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    
                    .build();

            // Create HTTP client (used to send the request)
            HttpClient client = HttpClient.newHttpClient();

            // Send the request and get response as a String
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            // parse it to extract only the text
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            String output = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();

        return output;

        } 
        catch (Exception e) {
            // If anything fails (network, bad request, etc.), return error message
            return "Error calling Gemini: " + e.getMessage();
        }
    }
}