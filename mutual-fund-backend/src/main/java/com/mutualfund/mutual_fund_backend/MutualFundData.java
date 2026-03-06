package com.mutualfund.mutual_fund_backend;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Paths;

public class MutualFundData {

    private final JsonObject data;

    public MutualFundData(String ticker) throws Exception {
        String scriptPath = Paths.get("scripts/fetch_fund_history.py").toAbsolutePath().toString();

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
            while ((line = err.readLine()) != null) System.err.println("[py] " + line);
        }

        int exit = process.waitFor();
        if (exit != 0 || json.isEmpty()) {
            throw new IOException("Python script failed (exit code " + exit + ")");
        }

        this.data = JsonParser.parseString(json).getAsJsonObject();
    }

    public double getAverageChange() {
        return data.get("averageChangePercent").getAsDouble();
    }

    public JsonArray getPeriods() {
        return data.getAsJsonArray("periods");
    }
}
