package com.mutualfund.mutual_fund_backend;

import java.util.Optional;

/**
 * Business logic for mutual fund (eg. calculations)
 */
public class MutualFundService {

    private final NewtonStockBetaClient newtonStockBetaClient = new NewtonStockBetaClient();

    //TODO: fxn to calculate future value (given formula)
    public double calculateFutureVal(String ticker, double principal, int years) {
        double presentValue = principal;
        double returnRate = calculateExpectedReturnRate(ticker, principal, years);
        double futureValue = presentValue * Math.pow((1 + returnRate), years);
        return futureValue; 
    }
    //TODO: fxn to calculate expected return rate from last year of mutual fund data
    public double calculateExpectedReturnRate(String ticker, double principal, int years) {
        double expectedReturnRate = 0; //TODO replace
        Optional<Double> beta = getBetaFromNewtonApi(ticker);
        double riskFreeRate = calculateRiskFreeRate();
        double marketReturn = calculateMarketReturn(); 
        if (beta.isPresent()) {
            expectedReturnRate = riskFreeRate + beta.get() * (marketReturn - riskFreeRate);
        }
        return expectedReturnRate;
    }
    private double calculateRiskFreeRate() {        
        return 0.02; //TODO replace with actual risk free rate
    }
    private double calculateMarketReturn() {        
        return 0.07; //TODO replace with actual market return
    }

    // fxn to get beta from Newton API 
    public Optional<Double> getBetaFromNewtonApi(String ticker) {
        return newtonStockBetaClient.getBeta(ticker);
    }
}
