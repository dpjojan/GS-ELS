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
    private Double alpha;
    private Double beta1;
    private Double beta2;

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

    public Double getAlpha() {
        return alpha;
    }

    public Double getBeta1() {
        return beta1;
    }

    public Double getBeta2() {
        return beta2;
    }

    public void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public void setBeta1(Double beta1) {
        this.beta1 = beta1;
    }

    public void setBeta2(Double beta2) {
        this.beta2 = beta2;
    }
}
