package com.mutualfund.mutual_fund_backend;

import java.util.Optional;

/**
 * Business logic for mutual fund (eg. calculations)
 */
public class MutualFundService {

    private final NewtonStockBetaClient newtonStockBetaClient = new NewtonStockBetaClient();

    //TODO: fxn to calculate future value (given formula)
    public double calculateFutureVal(String ticker, double principal, int years) {
        double presentValue = 0; //TODO replace
        double returnRate = 0; // TODO replace
        double futureValue = Math.pow(presentValue * (1 + returnRate),years);
        return futureValue; 
    }
    //TODO: fxn to calculate expected return rate from last year of mutual fund data
    public double calculateExpectedReturnRate(String ticker, double principal, int years) {
        
        double expectedReturnRate = 0; //TODO replace
        return expectedReturnRate;
    }
    
    

    // fxn to get beta from Newton API 
    public Optional<Double> getBetaFromNewtonApi(String ticker) {
        return newtonStockBetaClient.getBeta(ticker);
    }
}
