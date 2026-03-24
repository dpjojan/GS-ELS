package com.mutualfund.mutual_fund_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * Business logic for mutual fund (eg. calculations)
 */

@Service
public class MutualFundService {

    private static final Logger log = LoggerFactory.getLogger(MutualFundService.class);

    private final NewtonStockBetaClient newtonStockBetaClient = new NewtonStockBetaClient();
    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    public List<Double> calculateFutureValAllYears(String ticker, double principal, int years) {
        double rate = getrate(ticker);
        List<Double> results = new ArrayList<>();
        for (int y = 1; y <= years; y++) {
            results.add(principal * Math.exp(rate * y));
        }
        log.info("calculateFutureValAllYears - ticker={}, principal={}, years={}", ticker, principal, years);
        return results;
    }

    private double getrate(String ticker) {
        return rateCache.computeIfAbsent(ticker, t -> {
            log.info("Rate cache miss for {} - fetching rate", t);
            double beta = getBetaFromNewtonApi(t).orElse(1.0);
            double rate = 0.049 + beta * (calculateExpectedReturnRate(t) - 0.049);
            log.info("Computed rate for {} - beta={}, rate={}", t, beta, rate);
            return rate;
        });
    }

    private double calculateExpectedReturnRate(String ticker) {
        try {
            MutualFundData fundData = new MutualFundData(ticker);
            double rate = fundData.getAverageChange() / 100.0;
            log.info("Market return for {}: {}", ticker, rate);
            return rate;
        } catch (Exception e) {
            log.error("Failed to fetch market return for {}: {}", ticker, e.getMessage());
            throw new RuntimeException("Failed to fetch market return for " + ticker, e);
        }
    }

    // fxn to get beta from Newton API 
    public Optional<Double> getBetaFromNewtonApi(String ticker) {
        return newtonStockBetaClient.getBeta(ticker);
    }
}
