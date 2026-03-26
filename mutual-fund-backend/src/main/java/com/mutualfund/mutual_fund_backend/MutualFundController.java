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
        // US Equity
        fundsList.add(new MutualFund("TRBCX", "T. Rowe Price Blue Chip Growth"));
        fundsList.add(new MutualFund("AGTHX", "American Funds Growth Fund of America"));
        fundsList.add(new MutualFund("DODGX", "Dodge & Cox Stock Fund"));
        fundsList.add(new MutualFund("FCNTX", "Fidelity Contrafund"));
        fundsList.add(new MutualFund("FDGRX", "Fidelity Growth Company Fund"));
        fundsList.add(new MutualFund("PRGFX", "T. Rowe Price Growth Stock Fund"));
        fundsList.add(new MutualFund("VDIGX", "Vanguard Dividend Growth Fund"));
        fundsList.add(new MutualFund("AWSHX", "American Funds Washington Mutual"));
        fundsList.add(new MutualFund("PRBLX", "Parnassus Core Equity Fund"));
        fundsList.add(new MutualFund("JENSX", "Jensen Quality Growth Fund"));
        fundsList.add(new MutualFund("GABAX", "Gabelli Asset Fund"));
        fundsList.add(new MutualFund("CSIEX", "Calvert Equity Fund"));

        // Index
        fundsList.add(new MutualFund("VFIAX", "Vanguard 500 Index Fund Admiral"));
        fundsList.add(new MutualFund("FXAIX", "Fidelity 500 Index Fund"));
        fundsList.add(new MutualFund("SWPPX", "Schwab S&P 500 Index Fund"));
        fundsList.add(new MutualFund("VIGAX", "Vanguard Growth Index Admiral"));
        fundsList.add(new MutualFund("VTSAX", "Vanguard Total Stock Market Index Admiral"));
        fundsList.add(new MutualFund("FSKAX", "Fidelity Total Market Index Fund"));
        fundsList.add(new MutualFund("SWTSX", "Schwab Total Stock Market Index"));

        // Small/Mid Cap
        fundsList.add(new MutualFund("VIMAX", "Vanguard Mid-Cap Index Admiral"));
        fundsList.add(new MutualFund("FMCSX", "Fidelity Mid-Cap Stock Fund"));
        fundsList.add(new MutualFund("RPMGX", "T. Rowe Price Mid-Cap Growth Fund"));
        fundsList.add(new MutualFund("VSMAX", "Vanguard Small-Cap Index Admiral"));
        fundsList.add(new MutualFund("RYTRX", "Royce Total Return Fund"));
        fundsList.add(new MutualFund("BIASX", "Brown Advisory Small-Cap Growth"));

        // International
        fundsList.add(new MutualFund("VTIAX", "Vanguard Total International Stock Index"));
        fundsList.add(new MutualFund("AEPGX", "American Funds EuroPacific Growth"));
        fundsList.add(new MutualFund("FIGFX", "Fidelity International Growth Fund"));
        fundsList.add(new MutualFund("PRITX", "T. Rowe Price International Stock Fund"));
        fundsList.add(new MutualFund("OAKIX", "Oakmark International Fund"));
        fundsList.add(new MutualFund("DODFX", "Dodge & Cox International Stock Fund"));
        fundsList.add(new MutualFund("VTMGX", "Vanguard Developed Markets Index Admiral"));
        fundsList.add(new MutualFund("MAPIX", "Matthews Asia Dividend Fund"));
        fundsList.add(new MutualFund("BEXFX", "Baron Emerging Markets Fund"));
        fundsList.add(new MutualFund("FEMKX", "Fidelity Emerging Markets Fund"));

        // Sector
        fundsList.add(new MutualFund("VGHAX", "Vanguard Health Care Fund Admiral"));
        fundsList.add(new MutualFund("FSPTX", "Fidelity Select Technology Portfolio"));
        fundsList.add(new MutualFund("FSPHX", "Fidelity Select Health Care Portfolio"));
        fundsList.add(new MutualFund("PRHSX", "T. Rowe Price Health Sciences Fund"));
        fundsList.add(new MutualFund("FSUTX", "Fidelity Select Utilities Portfolio"));
        fundsList.add(new MutualFund("VGELX", "Vanguard Energy Fund Admiral"));
        fundsList.add(new MutualFund("FIDSX", "Fidelity Select Financial Services"));
        fundsList.add(new MutualFund("BREIX", "Baron Real Estate Fund"));
        fundsList.add(new MutualFund("VGSLX", "Vanguard Real Estate Index Admiral"));
        fundsList.add(new MutualFund("AREEX", "American Century Real Estate Fund"));
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
