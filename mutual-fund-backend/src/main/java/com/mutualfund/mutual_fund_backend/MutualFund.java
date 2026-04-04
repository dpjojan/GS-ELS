package com.mutualfund.mutual_fund_backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Class for a mutual fund (includes ticker, name of fund)
 */
@Entity
public class MutualFund {
    @Id
    private String ticker;
    private String name;
    private String risk;

    public MutualFund() {}

    public MutualFund(String ticker, String name, String risk) {
        this.ticker = ticker;
        this.name = name;
        this.risk = risk;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public String getRisk() {
        return risk;
    }
}
