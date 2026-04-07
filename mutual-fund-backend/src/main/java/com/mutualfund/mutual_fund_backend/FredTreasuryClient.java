package com.mutualfund.mutual_fund_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Client for FRED (Federal Reserve Economic Data) Treasury yield API.
 * Fetches the 10-year Treasury yield to use as the risk-free rate in CAPM.
 */
public class FredTreasuryClient {

    private static final Logger log = LoggerFactory.getLogger(FredTreasuryClient.class);
    private static final String BASE_URL = "https://api.stlouisfed.org/fred/series/observations";
    private static final String SERIES_ID = "DGS10";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Cached value so we only call FRED once per app startup
    private Double cachedYield = null;

    public FredTreasuryClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private Double cachedTermFactor   = null;
    private Double cachedCreditFactor = null;

    // Term Factor = 10-year Treasury minus 3-month T-bill (interest rate risk)
    public Optional<Double> getTermFactor() {
        if (cachedTermFactor != null) return Optional.of(cachedTermFactor);
        Optional<Double> tenYear    = getRiskFreeRate();
        Optional<Double> threeMonth = fetchSeries("DGS3MO");
        if (tenYear.isEmpty() || threeMonth.isEmpty()) return Optional.empty();
        cachedTermFactor = tenYear.get() - threeMonth.get();
        log.info("Term factor (DGS10 - DGS3MO): {}", cachedTermFactor);
        return Optional.of(cachedTermFactor);
    }

    // Credit Factor = Moody's BAA corporate yield minus 10-year Treasury (default risk)
    public Optional<Double> getCreditFactor() {
        if (cachedCreditFactor != null) return Optional.of(cachedCreditFactor);
        Optional<Double> baa     = fetchSeries("BAA");
        Optional<Double> tenYear = getRiskFreeRate();
        if (baa.isEmpty() || tenYear.isEmpty()) return Optional.empty();
        cachedCreditFactor = baa.get() - tenYear.get();
        log.info("Credit factor (BAA - DGS10): {}", cachedCreditFactor);
        return Optional.of(cachedCreditFactor);
    }

    private Optional<Double> fetchSeries(String seriesId) {
        String url = BASE_URL
                + "?series_id=" + seriesId
                + "&api_key=" + apiKey
                + "&sort_order=desc&limit=1&file_type=json";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.error("FRED API returned status {} for {}", response.statusCode(), seriesId);
                return Optional.empty();
            }
            JsonNode root         = objectMapper.readTree(response.body());
            JsonNode observations = root.path("observations");
            if (!observations.isArray() || observations.isEmpty()) return Optional.empty();
            String valueStr = observations.get(0).path("value").asText();
            if (valueStr.equals(".")) return Optional.empty();
            return Optional.of(Double.parseDouble(valueStr) / 100.0);
        } catch (Exception e) {
            log.error("Failed to fetch FRED {}: {}", seriesId, e.getMessage());
            return Optional.empty();
        }
    }

    // Returns the 10-year Treasury yield as a decimal (e.g. 0.0421 for 4.21%)
    // Used as the risk-free rate in CAPM
    public Optional<Double> getRiskFreeRate() {
        if (cachedYield != null) {
            log.info("Cache hit for FRED {}: {}", SERIES_ID, cachedYield);
            return Optional.of(cachedYield);
        }

        String url = BASE_URL
                + "?series_id=" + SERIES_ID
                + "&api_key=" + apiKey
                + "&sort_order=desc&limit=1&file_type=json";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                log.error("FRED API returned status {}", response.statusCode());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode observations = root.path("observations");

            if (!observations.isArray() || observations.isEmpty()) {
                log.error("No observations returned from FRED for {}", SERIES_ID);
                return Optional.empty();
            }

            String valueStr = observations.get(0).path("value").asText();

            // FRED returns "." when data is missing for that date (e.g. weekends)
            if (valueStr.equals(".")) {
                log.warn("FRED returned missing value for {}", SERIES_ID);
                return Optional.empty();
            }

            // FRED returns percentage e.g. 4.21, convert to decimal 0.0421
            cachedYield = Double.parseDouble(valueStr) / 100.0;
            log.info("Fetched risk-free rate from FRED: {} ({}%)", cachedYield, valueStr);
            return Optional.of(cachedYield);

        } catch (Exception e) {
            log.error("Failed to fetch FRED {}: {}", SERIES_ID, e.getMessage());
            return Optional.empty();
        }
    }
}
