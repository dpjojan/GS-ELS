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
        // Low Risk
        fundsList.add(new MutualFund("FZILX", "Fidelity ZERO International Index Fund", "Low"));
        fundsList.add(new MutualFund("PRDGX", "T. Rowe Price Dividend Growth Fund", "Low"));

        // Medium Risk
        fundsList.add(new MutualFund("VFIAX", "Vanguard 500 Index Fund Admiral", "Medium"));
        fundsList.add(new MutualFund("FXAIX", "Fidelity 500 Index Fund", "Medium"));

        // High Risk
        fundsList.add(new MutualFund("VSMAX", "Vanguard Small-Cap Index Admiral", "High"));
        fundsList.add(new MutualFund("SWLGX", "Schwab Large Cap Growth Fund", "High"));

        // US Equity
        fundsList.add(new MutualFund("TRBCX", "T. Rowe Price Blue Chip Growth", "High"));
        fundsList.add(new MutualFund("AGTHX", "American Funds Growth Fund of America", "High"));
        fundsList.add(new MutualFund("DODGX", "Dodge & Cox Stock Fund", "Medium"));
        fundsList.add(new MutualFund("FCNTX", "Fidelity Contrafund", "High"));
        fundsList.add(new MutualFund("FDGRX", "Fidelity Growth Company Fund", "High"));
        fundsList.add(new MutualFund("PRGFX", "T. Rowe Price Growth Stock Fund", "High"));
        fundsList.add(new MutualFund("VDIGX", "Vanguard Dividend Growth Fund", "Medium"));
        fundsList.add(new MutualFund("AWSHX", "American Funds Washington Mutual", "Medium"));
        fundsList.add(new MutualFund("PRBLX", "Parnassus Core Equity Fund", "Medium"));
        fundsList.add(new MutualFund("JENSX", "Jensen Quality Growth Fund", "Medium"));
        fundsList.add(new MutualFund("GABAX", "Gabelli Asset Fund", "Medium"));
        fundsList.add(new MutualFund("CSIEX", "Calvert Equity Fund", "Medium"));

        // Index
        fundsList.add(new MutualFund("SWPPX", "Schwab S&P 500 Index Fund", "Medium"));
        fundsList.add(new MutualFund("VIGAX", "Vanguard Growth Index Admiral", "High"));
        fundsList.add(new MutualFund("VTSAX", "Vanguard Total Stock Market Index Admiral", "Medium"));
        fundsList.add(new MutualFund("FSKAX", "Fidelity Total Market Index Fund", "Medium"));
        fundsList.add(new MutualFund("SWTSX", "Schwab Total Stock Market Index", "Medium"));

        // Small/Mid Cap
        fundsList.add(new MutualFund("VIMAX", "Vanguard Mid-Cap Index Admiral", "Medium"));
        fundsList.add(new MutualFund("FMCSX", "Fidelity Mid-Cap Stock Fund", "High"));
        fundsList.add(new MutualFund("RPMGX", "T. Rowe Price Mid-Cap Growth Fund", "High"));
        fundsList.add(new MutualFund("RYTRX", "Royce Total Return Fund", "High"));
        fundsList.add(new MutualFund("BIASX", "Brown Advisory Small-Cap Growth", "High"));

        // International
        fundsList.add(new MutualFund("VTIAX", "Vanguard Total International Stock Index", "Medium"));
        fundsList.add(new MutualFund("AEPGX", "American Funds EuroPacific Growth", "High"));
        fundsList.add(new MutualFund("FIGFX", "Fidelity International Growth Fund", "High"));
        fundsList.add(new MutualFund("PRITX", "T. Rowe Price International Stock Fund", "High"));
        fundsList.add(new MutualFund("OAKIX", "Oakmark International Fund", "High"));
        fundsList.add(new MutualFund("DODFX", "Dodge & Cox International Stock Fund", "Medium"));
        fundsList.add(new MutualFund("VTMGX", "Vanguard Developed Markets Index Admiral", "Medium"));
        fundsList.add(new MutualFund("MAPIX", "Matthews Asia Dividend Fund", "High"));
        fundsList.add(new MutualFund("BEXFX", "Baron Emerging Markets Fund", "High"));
        fundsList.add(new MutualFund("FEMKX", "Fidelity Emerging Markets Fund", "High"));

        // Sector
        fundsList.add(new MutualFund("VGHAX", "Vanguard Health Care Fund Admiral", "Medium"));
        fundsList.add(new MutualFund("FSPTX", "Fidelity Select Technology Portfolio", "High"));
        fundsList.add(new MutualFund("FSPHX", "Fidelity Select Health Care Portfolio", "High"));
        fundsList.add(new MutualFund("PRHSX", "T. Rowe Price Health Sciences Fund", "High"));
        fundsList.add(new MutualFund("FSUTX", "Fidelity Select Utilities Portfolio", "Low"));
        fundsList.add(new MutualFund("VGELX", "Vanguard Energy Fund Admiral", "High"));
        fundsList.add(new MutualFund("FIDSX", "Fidelity Select Financial Services", "High"));
        fundsList.add(new MutualFund("BREIX", "Baron Real Estate Fund", "High"));
        fundsList.add(new MutualFund("VGSLX", "Vanguard Real Estate Index Admiral", "Medium"));
        fundsList.add(new MutualFund("AREEX", "American Century Real Estate Fund", "Medium"));

        // Core Bond Funds
        fundsList.add(new MutualFund("BND", "Vanguard Total Bond Market ETF", "Low"));
        fundsList.add(new MutualFund("AGG", "iShares Core US Aggregate Bond ETF", "Low"));
        fundsList.add(new MutualFund("FBND", "Fidelity Total Bond ETF", "Low"));
        fundsList.add(new MutualFund("SCHZ", "Schwab US Aggregate Bond ETF", "Low"));
        fundsList.add(new MutualFund("JCBUX", "JPMorgan Core Bond Fund", "Low"));
        fundsList.add(new MutualFund("BAGIX", "Baird Aggregate Bond Fund", "Low"));
        fundsList.add(new MutualFund("DODIX", "Dodge & Cox Income Fund", "Low"));

        // Intermediate Core Bond Funds
        fundsList.add(new MutualFund("VCOBX", "Vanguard Core Bond Fund", "Low"));
        fundsList.add(new MutualFund("FBNDX", "Fidelity Investment Grade Bond Fund", "Low"));
        fundsList.add(new MutualFund("CBFYX", "Columbia Bond Fund", "Low"));
        fundsList.add(new MutualFund("WTRIX", "Allspring Core Bond Fund", "Low"));
        fundsList.add(new MutualFund("NRCRX", "Neuberger Berman Core Bond Fund", "Low"));

        // Specialized Bond Funds
        fundsList.add(new MutualFund("VCLT", "Vanguard Long-Term Corporate Bond ETF", "Medium"));
        fundsList.add(new MutualFund("PRFRX", "T. Rowe Price Floating Rate Fund", "Medium"));
        fundsList.add(new MutualFund("HSNFX", "Hartford Strategic Income Fund", "Medium"));
        fundsList.add(new MutualFund("APDFX", "Artisan High Income Fund", "High"));

        // Municipal Bond Funds (Tax-Exempt)
        fundsList.add(new MutualFund("VTEB", "Vanguard Tax-Exempt Bond ETF", "Low"));
        fundsList.add(new MutualFund("VWIUX", "Vanguard Intermediate-Term Tax-Exempt Fund", "Low"));
        fundsList.add(new MutualFund("TEAFX", "American Funds Tax-Exempt Bond Fund", "Low"));
        fundsList.add(new MutualFund("AHMFX", "American High-Income Municipal Bond Fund", "Medium"));

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
        return geminiService.chat(message);
    }


}
