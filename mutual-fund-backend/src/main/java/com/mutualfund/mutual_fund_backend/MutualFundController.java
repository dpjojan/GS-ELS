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

    // TODO: hardcode list of mutual funds
    // when frontend requests mutual funds, return this list of hardcoded funds
    @GetMapping("/funds")
    public ArrayList<MutualFund> getFunds() {
        ArrayList<MutualFund> fundsList = new ArrayList<>();
        fundsList.add(new MutualFund("ticker1", "fund1"));
        fundsList.add(new MutualFund("ticker2", "fund2"));
        fundsList.add(new MutualFund("ticker3", "fund3"));
        fundsList.add(new MutualFund("ticker3", "fund4"));
        return fundsList;
    }
    
    /*TODO: GET future value of investment amount (call fxn in MutualFundService and pass on to frontend)
    - depends on MutualFund, MutualFundService being completed
     */
    // when frontend requests future val of a ticker given principal and length of time, return the expected future value
    @GetMapping("/futureVal")
    public double getFutureVal( //TODO change to Map<String,Object>?
        @RequestParam String ticker,
        @RequestParam double principal,
        @RequestParam int years){
            return mutualFundService.calculateFutureVal(ticker, principal, years);
    }


}
