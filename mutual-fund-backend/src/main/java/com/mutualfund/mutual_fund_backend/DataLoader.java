package com.mutualfund.mutual_fund_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with mutual funds on startup if the table is empty.
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private MutualFundRepository mutualFundRepository;

    @Override
    public void run(String... args) {
        if (mutualFundRepository.count() > 0) return;

        mutualFundRepository.saveAll(List.of(
            // Low Risk
            new MutualFund("FZILX", "Fidelity ZERO International Index Fund", "Low"),
            new MutualFund("PRDGX", "T. Rowe Price Dividend Growth Fund", "Low"),
            new MutualFund("FSUTX", "Fidelity Select Utilities Portfolio", "Low"),
            // Medium Risk
            new MutualFund("VFIAX", "Vanguard 500 Index Fund Admiral", "Medium"),
            new MutualFund("FXAIX", "Fidelity 500 Index Fund", "Medium"),
            new MutualFund("DODGX", "Dodge & Cox Stock Fund", "Medium"),
            new MutualFund("VDIGX", "Vanguard Dividend Growth Fund", "Medium"),
            new MutualFund("AWSHX", "American Funds Washington Mutual", "Medium"),
            new MutualFund("PRBLX", "Parnassus Core Equity Fund", "Medium"),
            new MutualFund("JENSX", "Jensen Quality Growth Fund", "Medium"),
            new MutualFund("GABAX", "Gabelli Asset Fund", "Medium"),
            new MutualFund("CSIEX", "Calvert Equity Fund", "Medium"),
            new MutualFund("SWPPX", "Schwab S&P 500 Index Fund", "Medium"),
            new MutualFund("VTSAX", "Vanguard Total Stock Market Index Admiral", "Medium"),
            new MutualFund("FSKAX", "Fidelity Total Market Index Fund", "Medium"),
            new MutualFund("SWTSX", "Schwab Total Stock Market Index", "Medium"),
            new MutualFund("VIMAX", "Vanguard Mid-Cap Index Admiral", "Medium"),
            new MutualFund("VTIAX", "Vanguard Total International Stock Index", "Medium"),
            new MutualFund("DODFX", "Dodge & Cox International Stock Fund", "Medium"),
            new MutualFund("VTMGX", "Vanguard Developed Markets Index Admiral", "Medium"),
            new MutualFund("VGHAX", "Vanguard Health Care Fund Admiral", "Medium"),
            new MutualFund("VGSLX", "Vanguard Real Estate Index Admiral", "Medium"),
            new MutualFund("AREEX", "American Century Real Estate Fund", "Medium"),
            // High Risk
            new MutualFund("VSMAX", "Vanguard Small-Cap Index Admiral", "High"),
            new MutualFund("SWLGX", "Schwab Large Cap Growth Fund", "High"),
            new MutualFund("TRBCX", "T. Rowe Price Blue Chip Growth", "High"),
            new MutualFund("AGTHX", "American Funds Growth Fund of America", "High"),
            new MutualFund("FCNTX", "Fidelity Contrafund", "High"),
            new MutualFund("FDGRX", "Fidelity Growth Company Fund", "High"),
            new MutualFund("PRGFX", "T. Rowe Price Growth Stock Fund", "High"),
            new MutualFund("VIGAX", "Vanguard Growth Index Admiral", "High"),
            new MutualFund("FMCSX", "Fidelity Mid-Cap Stock Fund", "High"),
            new MutualFund("RPMGX", "T. Rowe Price Mid-Cap Growth Fund", "High"),
            new MutualFund("RYTRX", "Royce Total Return Fund", "High"),
            new MutualFund("BIASX", "Brown Advisory Small-Cap Growth", "High"),
            new MutualFund("AEPGX", "American Funds EuroPacific Growth", "High"),
            new MutualFund("FIGFX", "Fidelity International Growth Fund", "High"),
            new MutualFund("PRITX", "T. Rowe Price International Stock Fund", "High"),
            new MutualFund("OAKIX", "Oakmark International Fund", "High"),
            new MutualFund("MAPIX", "Matthews Asia Dividend Fund", "High"),
            new MutualFund("BEXFX", "Baron Emerging Markets Fund", "High"),
            new MutualFund("FEMKX", "Fidelity Emerging Markets Fund", "High"),
            new MutualFund("FSPTX", "Fidelity Select Technology Portfolio", "High"),
            new MutualFund("FSPHX", "Fidelity Select Health Care Portfolio", "High"),
            new MutualFund("PRHSX", "T. Rowe Price Health Sciences Fund", "High"),
            new MutualFund("VGELX", "Vanguard Energy Fund Admiral", "High"),
            new MutualFund("FIDSX", "Fidelity Select Financial Services", "High"),
            new MutualFund("BREIX", "Baron Real Estate Fund", "High")
        ));
    }
}
