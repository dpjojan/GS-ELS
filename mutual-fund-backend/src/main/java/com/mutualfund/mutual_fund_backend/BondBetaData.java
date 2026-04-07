package com.mutualfund.mutual_fund_backend;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;

/**
 * Calls fetch_bond_betas.py and parses the resulting alpha, beta1, beta2 for a bond fund.
 */
public class BondBetaData {

    private static final Logger log = LoggerFactory.getLogger(BondBetaData.class);

    private final JsonObject data;

    public BondBetaData(String ticker) throws Exception {
        String scriptPath = Paths.get("scripts/fetch_bond_betas.py").toAbsolutePath().toString();

        ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, ticker.toUpperCase());
        pb.directory(new File(scriptPath).getParentFile());
        pb.redirectErrorStream(false);

        Process process = pb.start();

        String json;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            json = sb.toString();
        }

        try (BufferedReader err = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = err.readLine()) != null) log.warn("[py] {}", line);
        }

        int exit = process.waitFor();
        if (exit != 0 || json.isEmpty()) {
            throw new IOException("fetch_bond_betas.py failed for " + ticker + " (exit code " + exit + ")");
        }

        this.data = JsonParser.parseString(json).getAsJsonObject();
    }

    public double getAlpha() {
        return data.get("alpha").getAsDouble();
    }

    public double getBeta1() {
        return data.get("beta1").getAsDouble();
    }

    public double getBeta2() {
        return data.get("beta2").getAsDouble();
    }
}
