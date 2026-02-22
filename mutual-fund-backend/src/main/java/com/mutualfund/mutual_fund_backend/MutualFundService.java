package com.mutualfund.mutual_fund_backend;

import java.util.Optional;

/**
 * Business logic for mutual fund (eg. calculations)
 */
public class MutualFundService {

    private final NewtonStockBetaClient newtonStockBetaClient = new NewtonStockBetaClient();

    //TODO: fxn to calculate future value (given formula)

    //TODO: fxn to calculate expected return rate from last year of mutual fund data

    // fxn to get beta from Newton API
    public Optional<Double> getBetaFromNewtonApi(String ticker) {
        return newtonStockBetaClient.getBeta(ticker);
    }
}
