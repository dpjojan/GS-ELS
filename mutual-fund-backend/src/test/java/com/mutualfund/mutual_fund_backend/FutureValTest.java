package com.mutualfund.mutual_fund_backend;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FutureValTest {

    @Test
    void futureValForVFIAX() {
        MutualFundService service = new MutualFundService();

        double principal = 10_000.0;
        int years = 10;
        List<Double> results = service.calculateFutureValAllYears("VFIAX", principal, years);

        System.out.println("Ticker    : VFIAX");
        System.out.println("Principal : $" + principal);
        System.out.println("Years     : " + years);
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("Year %2d   : $%.2f%n", i + 1, results.get(i));
        }

        assertEquals(years, results.size());
        results.forEach(val -> {
            assertTrue(val > 0);
            assertFalse(Double.isNaN(val));
        });
    }
}
