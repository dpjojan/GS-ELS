package com.mutualfund.mutual_fund_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds bond funds and populates Fama-French betas for any that haven't been calculated yet.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private MutualFundRepository mutualFundRepository;

    @Override
    public void run(String... args) {
        seedBondFunds();
        new Thread(this::populateMissingBetas, "beta-loader").start();
    }

    private void seedBondFunds() {
        List<MutualFund> bondFunds = List.of(
            new MutualFund("BND",   "Vanguard Total Bond Market ETF",             "Low"),
            new MutualFund("AGG",   "iShares Core U.S. Aggregate Bond ETF",       "Low"),
            new MutualFund("FBND",  "Fidelity Total Bond ETF",                    "Low"),
            new MutualFund("SCHZ",  "Schwab U.S. Aggregate Bond ETF",             "Low"),
            new MutualFund("JCBUX", "JPMorgan Core Bond Fund",                    "Low"),
            new MutualFund("BAGIX", "Baird Aggregate Bond Fund",                  "Low"),
            new MutualFund("DODIX", "Dodge & Cox Income Fund",                    "Low"),
            new MutualFund("VCOBX", "Vanguard Core Bond Fund",                    "Low"),
            new MutualFund("FBNDX", "Fidelity Investment Grade Bond Fund",        "Low"),
            new MutualFund("CBFYX", "Columbia Bond Fund",                         "Low"),
            new MutualFund("WTRIX", "Western Asset Core Bond Fund",               "Low"),
            new MutualFund("NRCRX", "Neuberger Berman Core Bond Fund",            "Low"),
            new MutualFund("VCLT",  "Vanguard Long-Term Corporate Bond ETF",      "Medium"),
            new MutualFund("PRFRX", "T. Rowe Price Floating Rate Fund",           "Medium"),
            new MutualFund("HSNFX", "Homestead Short-Term Bond Fund",             "Low"),
            new MutualFund("APDFX", "American Funds Preservation Portfolio",      "Low"),
            new MutualFund("VTEB",  "Vanguard Tax-Exempt Bond ETF",               "Low"),
            new MutualFund("VWIUX", "Vanguard Intermediate-Term Tax-Exempt Fund", "Low"),
            new MutualFund("TEAFX", "T. Rowe Price Tax-Free Income Fund",         "Low"),
            new MutualFund("AHMFX", "American High Income Municipal Fund",        "Medium")
        );
        bondFunds.stream()
            .filter(f -> !mutualFundRepository.existsById(f.getTicker()))
            .forEach(mutualFundRepository::save);
    }

    private void populateMissingBetas() {
        mutualFundRepository.findAll().stream()
            .filter(f -> f.getBeta1() == null && isBondTicker(f.getTicker()))
            .forEach(fund -> {
                try {
                    log.info("Fetching betas for bond fund {}", fund.getTicker());
                    BondBetaData betaData = new BondBetaData(fund.getTicker());
                    fund.setAlpha(betaData.getAlpha());
                    fund.setBeta1(betaData.getBeta1());
                    fund.setBeta2(betaData.getBeta2());
                    mutualFundRepository.save(fund);
                    log.info("Saved betas for {} - alpha={}, beta1={}, beta2={}",
                            fund.getTicker(), fund.getAlpha(), fund.getBeta1(), fund.getBeta2());
                } catch (Exception e) {
                    log.error("Failed to fetch betas for {}: {}", fund.getTicker(), e.getMessage());
                }
            });
    }

    private boolean isBondTicker(String ticker) {
        return switch (ticker) {
            case "BND","AGG","FBND","SCHZ","JCBUX","BAGIX","DODIX",
                 "VCOBX","FBNDX","CBFYX","WTRIX","NRCRX","VCLT",
                 "PRFRX","HSNFX","APDFX","VTEB","VWIUX","TEAFX","AHMFX" -> true;
            default -> false;
        };
    }
}
