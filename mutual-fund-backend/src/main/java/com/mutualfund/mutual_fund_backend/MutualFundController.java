package com.mutualfund.mutual_fund_backend;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles frontend requests
 *
 */
@CrossOrigin(origins = "http://localhost:4200") //enable connection to angular frontend

@RestController
@RequestMapping("/api")
public class MutualFundController {
    
    @Autowired // SpringBoot creates an instance of MutualFundService to use
    private MutualFundService mutualFundService;

    // Spring will automatically create and inject GeminiService
    // so we can use it inside this controller
    @Autowired
    private GeminiService geminiService;

    // when frontend requests mutual funds, return this list of hardcoded funds
    @GetMapping("/funds")
    public ArrayList<MutualFund> getFunds() {
        ArrayList<MutualFund> fundsList = new ArrayList<>();
        fundsList.add(new MutualFund("VBTLX", "Vanguard Total Bond Market Index Fund"));
        fundsList.add(new MutualFund("SWAGX", "Schwab US Aggregate Bond Index Fund"));
        fundsList.add(new MutualFund("VFIAX", "Vanguard 500 Index Fund"));
        fundsList.add(new MutualFund("FXAIX", "Fidelity 500 Index Fund"));
        fundsList.add(new MutualFund("VSMAX", "Vanguard Small Cap Index Fund"));
        fundsList.add(new MutualFund("SWLGX", "Schwab Large Cap Growth Fund"));
        return fundsList;
    }
    
    // when frontend requests future val of a ticker given principal and length of time, return the expected future value
    @GetMapping("/futureVal")
    public double getFutureVal( //change to Map<String,Object> if want to send additional information back
        @RequestParam String ticker,
        @RequestParam double principal,
        @RequestParam int years){
            return mutualFundService.calculateFutureVal(ticker, principal, years);
    }

    // maps HTTP GET requests to "/gemini-test"
    @GetMapping("/gemini-test")
    public String testGemini() {
    // Calling the geminiService to generate a response
    return geminiService.testGemini("Explain mutual funds in one sentence");
}


}
