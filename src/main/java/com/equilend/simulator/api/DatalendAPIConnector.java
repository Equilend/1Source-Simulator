package com.equilend.simulator.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.auth.DatalendToken;
import com.google.gson.Gson;

public class DatalendAPIConnector {

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static String restAPIURL = null;
    private static Gson gson = new Gson(); 
    private static final Logger logger = LogManager.getLogger();    

    public static void setRestAPIURL(String url){
        restAPIURL = url;
    }

    public static OffsetDateTime getCurrentTime() {
        return OffsetDateTime.now(ZoneId.of("UTC"));
    }
    
    public static String formatTime(OffsetDateTime time) {
        return time.format(formatter);
    }    

    public static double getSecurityPrice(DatalendToken token, String idType, String idValue) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get security price";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("Datalend REST API URL not properly loaded");
        }        

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/inddata/security/price?" + idType + "=" + idValue))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with security price get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending security price get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get Security Price: Status Code {}", getResponse.statusCode());

        SecurityResponse response = gson.fromJson(getResponse.body(), SecurityResponse.class);
        return response.getPrice();
    }  
    
    public static double getSecurityFee(DatalendToken token, String idType, String idValue) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get security average fee";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("Datalend REST API URL not properly loaded");
        }        

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/inddata/security/fee?bucket=1&" + idType + "=" + idValue))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with security average fee get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending security average fee get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get Security Price: Status Code {}", getResponse.statusCode());

        SecurityResponse response = gson.fromJson(getResponse.body(), SecurityResponse.class);
        return response.getAvgFee();
    }     

    public static double getSecurityRebate(DatalendToken token, String idType, String idValue) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get security average rebate";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("Datalend REST API URL not properly loaded");
        }        

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/inddata/security/rebate?bucket=1&" + idType + "=" + idValue))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with security average rebate get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending security average rebate get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get Security Price: Status Code {}", getResponse.statusCode());

        SecurityResponse response = gson.fromJson(getResponse.body(), SecurityResponse.class);
        return response.getAvgRebate();
    } 

}