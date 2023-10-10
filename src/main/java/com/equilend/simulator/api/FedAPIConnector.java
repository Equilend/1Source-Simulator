package com.equilend.simulator.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class FedAPIConnector {

    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger();

    public class RefRate {
        
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

    public class RefRates {
        
        private List<RefRate> refRates;

        public List<RefRate> getRefRates() {
            return refRates;
        }

        public void setRefRates(List<RefRate> refRates) {
            this.refRates = refRates;
        }

    }

    public static List<RefRate> getLatestRefRates() throws FedAPIException {
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

        RefRates latestJson = gson.fromJson(getResponse.body(), RefRates.class);
        for (RefRate refRate : latestJson.getRefRates()){
            logger.info("{}: {} on {}", refRate.getType(), refRate.getPercentRate(), refRate.getEffectiveDate());
        }
        return latestJson.getRefRates();
    }
}
