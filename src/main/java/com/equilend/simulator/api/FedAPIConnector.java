package com.equilend.simulator.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class FedAPIConnector {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger();
    private static Map<String, RefRate> refRates = null;

    public static class RefRate {
        
        private String effectiveDate;
        private String type;
        private Double percentRate;

        public String getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(String effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getPercentRate() {
            return percentRate;
        }

        public void setPercentRate(Double percentRate) {
            this.percentRate = percentRate;
        }

    }

    public static class RefRates {
        
        private List<RefRate> refRates;

        public List<RefRate> getRefRates() {
            return refRates;
        }

        public void setRefRates(List<RefRate> refRates) {
            this.refRates = refRates;
        }

    }

    private static void populateRefRates() throws FedAPIException {
        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://markets.newyorkfed.org/api/rates/all/latest.json"))
                .header("Accept", "application/json")
                .build();
        }
        catch (URISyntaxException e){
            String message = "URISyntax Error getting the latest reference rates from NYFed API";
            logger.error(message, e);
            throw new FedAPIException(message, e);
        }

        HttpResponse<String> getResponse;
        try{
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e){
            String message = "IO Error getting the latest reference rates from NYFed API";
            logger.error(message, e);
            throw new FedAPIException(message, e);
        }

        refRates = new HashMap<>();
        RefRates latestJson = gson.fromJson(getResponse.body(), RefRates.class);
        for (RefRate refRate : latestJson.getRefRates()){
            refRates.put(refRate.getType(), refRate);
        }
    }

    public static RefRate getRefRate(String benchmark) throws FedAPIException{
        if (refRates == null) populateRefRates();

        return refRates.get(benchmark);
    }
}
