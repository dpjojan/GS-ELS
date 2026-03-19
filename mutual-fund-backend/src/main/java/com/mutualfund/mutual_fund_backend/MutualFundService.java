package com.mutualfund.mutual_fund_backend;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * Business logic for mutual fund (eg. calculations)
 */

@Service
public class MutualFundService {

    private final NewtonStockBetaClient newtonStockBetaClient = new NewtonStockBetaClient();
    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    //TODO: fxn to calculate future value (given formula)
    public double calculateFutureVal(String ticker, double principal, int years) {
        double presentValue = principal;
        double futureValue = presentValue * Math.exp(getrate(ticker) * years);
        return futureValue;
    }

    private double getrate(String ticker) {
        return rateCache.computeIfAbsent(ticker, t -> {
            double beta = getBetaFromNewtonApi(t).orElse(1.0);
            return 0.049 + beta * (calculateExpectedReturnRate(t) - 0.049);
        });
    }
    private double calculateExpectedReturnRate(String ticker) {
        try {
            MutualFundData fundData = new MutualFundData(ticker);
            return fundData.getAverageChange() / 100.0;
        } catch (Exception e) {
            System.err.println("Failed to fetch market return for " + ticker + ": " + e.getMessage());
            return 0.0; // fallback
        }
    }

    // fxn to get beta from Newton API 
    public Optional<Double> getBetaFromNewtonApi(String ticker) {
        return newtonStockBetaClient.getBeta(ticker);
    }
}
