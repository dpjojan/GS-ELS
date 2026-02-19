package com.mutualfund.mutual_fund_backend;

/**
 * Class for a mutual fund (includes ticker, name of fund)
 */
public class MutualFund {
    private String ticker; //the unique identifiable feature for each mutual fund (needed for Newton API)
    private String name;

    public MutualFund(String ticker, String name) {
        this.ticker = ticker;
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }


}
