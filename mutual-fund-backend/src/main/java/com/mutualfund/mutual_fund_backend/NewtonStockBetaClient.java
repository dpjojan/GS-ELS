package com.mutualfund.mutual_fund_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Client for Newton Analytics Stock Beta API.
 * Docs: https://newtonanalytics.com/docs/api/stockbeta.php
 */
public class NewtonStockBetaClient {

    private static final String BASE_URL = "https://api.newtonanalytics.com/stock-beta/";
    private static final String DEFAULT_INDEX = "^GSPC";  // S&P 500
    private static final String DEFAULT_INTERVAL = "1mo";
    private static final int DEFAULT_OBSERVATIONS = 12;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NewtonStockBetaClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // Fetches beta for a ticker from Newton Analytics API.
    public Optional<Double> getBeta(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            return Optional.empty();
        }
        String url = BASE_URL + "?ticker=" + encode(ticker)
                + "&index=" + encode(DEFAULT_INDEX)
                + "&interval=" + encode(DEFAULT_INTERVAL)
                + "&observations=" + DEFAULT_OBSERVATIONS;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            int status = root.path("status").asInt(-1);
            if (status != 200) {
                return Optional.empty();
            }

            JsonNode data = root.path("data");
            if (data.isNumber()) {
                return Optional.of(data.asDouble());
            }
            if (data.isObject() && data.has("beta")) {
                return Optional.of(data.get("beta").asDouble());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
