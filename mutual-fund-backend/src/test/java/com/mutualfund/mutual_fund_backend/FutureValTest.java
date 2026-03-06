package com.mutualfund.mutual_fund_backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FutureValTest {

    @Test
    void futureValForVFIAX() {
        MutualFundService service = new MutualFundService();

        double principal = 10_000.0;
        int years = 10;
        double result = service.calculateFutureVal("VFIAX", principal, years);

        System.out.println("Ticker    : VFIAX");
        System.out.println("Principal : $" + principal);
        System.out.println("Years     : " + years);
        System.out.printf("Future Val: $%.2f%n", result);

        assertTrue(result > 0);
        assertFalse(Double.isNaN(result));
    }
}
