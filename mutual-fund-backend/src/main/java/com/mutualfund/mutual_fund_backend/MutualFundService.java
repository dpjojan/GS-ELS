package com.mutualfund.mutual_fund_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
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
    // bond fund list
    private static final Set<String> BOND_FUNDS = Set.of("BND","AGG", "FBND", "SCHZ", "JCBUX", "BAGIX", "DODIX","VCOBX","FBNDX","CBFYX","WTRIX","NRCRX","VCLT","PRFRX","HSNFX","APDFX","VTEB","VWIUX","TEAFX","AHMFX"); 
    private final NewtonStockBetaClient newtonStockBetaClient = new NewtonStockBetaClient();
    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    public List<Double> calculateFutureValAllYears(String ticker, double principal, int years) {
        double rate;
        List<Double> results = new ArrayList<>();
        if (isBondFund(ticker)) {
            rate = getbondrate(ticker);
            log.info("Calculating future value for bond fund {} - principal={}, years={}", ticker, principal, years);
            }
        else {
            rate = getrate(ticker);
            log.info("calculateFutureValAllYears - ticker={}, principal={}, years={}", ticker, principal, years);
            }
        for (int y = 1; y <= years; y++) {
            if (isBondFund(ticker)) {
                results.add(principal * Math.pow(1 + (rate/12), 12 * y));
            } 
            else {
                results.add(principal * Math.exp(rate * y));
                }
            }
        return results;
        }

    private boolean isBondFund(String ticker) {
        return BOND_FUNDS.contains(ticker);
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

        private double getbondrate(String ticker) {
            // pull from database
            
            double yFloor = 0.042; //getFloor(ticker);
            double yCeiling = 0.058; //getCeiling(ticker);
            double floorDuration = 3.0; //getDurationForFloor(ticker);
            double ceilingDuration = 7.0; //getDurationForCeiling(ticker);
            double trueDuration = 5.0; //getDuration(ticker);
            
            // in case duration is exactly on floor or ceiling, avoid division by zero and just use the corresponding rate
            if (ceilingDuration == floorDuration) {
                return yFloor;
            }
            else {
                return yFloor + ((yCeiling - yFloor) * ((trueDuration - floorDuration) / (ceilingDuration - floorDuration)));
                }
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
