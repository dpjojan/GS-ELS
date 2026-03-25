package com.mutualfund.mutual_fund_backend;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private static final Logger log = LoggerFactory.getLogger(MutualFundController.class);

    @Autowired // SpringBoot creates an instance of MutualFundService to use
    private MutualFundService mutualFundService;

    // Spring will automatically create and inject GeminiService
    // so we can use it inside this controller
    @Autowired
    private GeminiService geminiService;

    // when frontend requests mutual funds, return this list of hardcoded funds
    @GetMapping("/funds")
    public ArrayList<MutualFund> getFunds() {
        log.info("GET /funds requested");
        ArrayList<MutualFund> fundsList = new ArrayList<>();
        fundsList.add(new MutualFund("VFIAX", "Vanguard 500 Index Fund"));
        fundsList.add(new MutualFund("FXAIX", "Fidelity 500 Index Fund"));
        fundsList.add(new MutualFund("VSMAX", "Vanguard Small Cap Index Fund"));
        fundsList.add(new MutualFund("SWLGX", "Schwab Large Cap Growth Fund"));
        fundsList.add(new MutualFund("PRDGX", "T.Rowe Price Dividend Growth Fund"));
        fundsList.add(new MutualFund("FZILX", "Fidelity ZERO International Index Fund"));
        return fundsList;
    }
    
    @GetMapping("/futureValAll")
    public ResponseEntity<?> getFutureValAll(
        @RequestParam String ticker,
        @RequestParam double principal,
        @RequestParam int years){
            log.info("GET /futureValAll - ticker={}, principal={}, years={}", ticker, principal, years);
            try {
                return ResponseEntity.ok(mutualFundService.calculateFutureValAllYears(ticker, principal, years));
            } catch (RuntimeException e) {
                log.error("Failed to calculate future values for {}: {}", ticker, e.getMessage());
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
    }

    // maps HTTP GET requests to "/gemini-test"
    @GetMapping("/gemini-test")
    public String testGemini() {
        return geminiService.testGemini("Explain mutual funds in one sentence");
    }

    // receives a message from the Angular chatbot and returns Gemini's response
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        log.info("POST /chat - message=\"{}\"", message);
        return geminiService.testGemini(message);
    }


}
