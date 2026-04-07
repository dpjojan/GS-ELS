package com.mutualfund.mutual_fund_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
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

    @Value("${fred.api.key}")
    private String fredApiKey;

    @Autowired
    private MutualFundRepository mutualFundRepository;

    private FredTreasuryClient fredTreasuryClient;

    @PostConstruct
    public void init() {
        fredTreasuryClient = new FredTreasuryClient(fredApiKey);
    }

    public List<Double> calculateFutureValAllYears(String ticker, double principal, int years) {
        MutualFund fund = mutualFundRepository.findById(ticker)
                .orElseThrow(() -> new RuntimeException("Fund not found: " + ticker));
        double rate;
        List<Double> results = new ArrayList<>();
        if (isBondFund(fund)) {
            rate = getbondrate(fund);
            log.info("Calculating future value for bond fund {} - principal={}, years={}", ticker, principal, years);
        } else {
            rate = getrate(ticker);
            log.info("calculateFutureValAllYears - ticker={}, principal={}, years={}", ticker, principal, years);
        }
        for (int y = 1; y <= years; y++) {
            if (isBondFund(fund)) {
                results.add(principal * Math.pow(1 + (rate / 12), 12 * y));
            } else {
                results.add(principal * Math.exp(rate * y));
            }
        }
        return results;
    }

    private boolean isBondFund(MutualFund fund) {
        return fund.getBeta1() != null;
    }

    private double getrate(String ticker) {
        return rateCache.computeIfAbsent(ticker, t -> {
            log.info("Rate cache miss for {} - fetching rate", t);
            double riskFreeRate = fredTreasuryClient.getRiskFreeRate().orElse(0.049);
            double beta = getBetaFromNewtonApi(t).orElse(1.0);
            double rate = riskFreeRate + beta * (calculateExpectedReturnRate(t) - riskFreeRate);
            log.info("Computed rate for {} - riskFreeRate={}, beta={}, rate={}", t, riskFreeRate, beta, rate);
            return rate;
        });
    }

    private double getbondrate(MutualFund fund) {
        double termFactor    = fredTreasuryClient.getTermFactor().orElse(0.015);
        double creditFactor  = fredTreasuryClient.getCreditFactor().orElse(0.010);
        // regression was run on monthly returns, so Ri is a monthly rate
        double monthlyRate   = fund.getAlpha() + fund.getBeta1() * termFactor + fund.getBeta2() * creditFactor;
        // annualize so the compounding formula (1 + rate/12)^12t works correctly
        double annualRate    = Math.pow(1 + monthlyRate, 12) - 1;
        log.info("Fama-French bond rate for {} - alpha={}, beta1={}, beta2={}, term={}, credit={}, monthlyRate={}, annualRate={}",
                fund.getTicker(), fund.getAlpha(), fund.getBeta1(), fund.getBeta2(), termFactor, creditFactor, monthlyRate, annualRate);
        return annualRate;
    }

    public double getExpectedReturn(String ticker) {
        return calculateExpectedReturnRate(ticker);
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
