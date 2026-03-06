package com.mutualfund.mutual_fund_backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarketReturnTest {

    @Test
    void calculateMarketReturnForVBTLX() throws Exception {
        MutualFundData fundData = new MutualFundData("VBTLX");
        double marketReturn = fundData.getAverageChange() / 100.0;

        System.out.println("averageChangePercent : " + fundData.getAverageChange() + "%");
        System.out.println("calculateMarketReturn: " + marketReturn);

        assertFalse(Double.isNaN(marketReturn));
        assertFalse(Double.isInfinite(marketReturn));
    }
}
