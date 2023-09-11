package com.equilend.simulator.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.agreement.Agreement;
import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.contract.ContractProposal;
import com.equilend.simulator.event.Event;
import com.equilend.simulator.settlement.AcceptSettlement;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.token.Token;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class APIConnector {

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger();

    public static String encodeMapAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }

        return formBodyBuilder.toString();
    }
    
    public static Token getBearerToken(Map<String, String> loginInfo) throws APIException {
        if (loginInfo == null){
            throw new APIException("Login info not configured or failed to be read");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageauth.equilend.com/auth/realms/1Source/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString(encodeMapAsString(loginInfo)))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating token post request";
            logger.error(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending token post request";
            logger.error(message, e);
            throw new APIException(message, e);
        }

        Token token = gson.fromJson(postResponse.body(), Token.class);
        if (token.getError() != null){ 
            String message = "Error authorizing bearer token: " + token.getError_description();
            logger.error(message);
            throw new APIException(message);
        }

        return token;
    }

    public static OffsetDateTime getCurrentTime() {
        return OffsetDateTime.now(ZoneId.of("UTC"));
    }
    
    public static String formatTime(OffsetDateTime time) {
        return time.format(formatter);
    }    
   
    public static List<Event> getAllEvents(BearerToken token, OffsetDateTime since, int eventId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get all events";
            logger.error(message);
            throw new APIException(message);
        }

        String sinceStr = formatTime(since);
        String encodedSince = URLEncoder.encode(sinceStr, StandardCharsets.UTF_8);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/events" + "?" + "since=" + encodedSince + "&" + "fromEventId=" + eventId))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating events get request";
            logger.error(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending events get request";
            logger.error(message, e);
            throw new APIException(message, e);
        }

        logger.info("Get All Events: Status Code {}", getResponse.statusCode());

        Type eventListType = new TypeToken<ArrayList<Event>>(){}.getType();
        return gson.fromJson(getResponse.body(), eventListType);
    }     

    public static Agreement getAgreementById(BearerToken token, String id) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get agreement by id";
            logger.error(message);
            throw new APIException(message);
        }

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/agreements" + "/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error creating get request for agreement " + id;
            logger.error(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating get request for agreement " + id;
            logger.error(message, e);
            throw new APIException(message, e);
        }

        logger.info("Get Agreement By Id: Status Code {}", getResponse.statusCode());
        
        Agreement agreement = gson.fromJson(getResponse.body(), Agreement.class);
        return agreement;
    }

    public static Contract getContractById(BearerToken token, String id) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get contract by id";
            logger.error(message);
            throw new APIException(message);
        }

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts" + "/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error creating get request for contract " + id;
            logger.error(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating get request for contract " + id;
            logger.error(message, e);
            throw new APIException(message, e);
        }
       
        logger.info("Get Contract By Id: Status Code {}", getResponse.statusCode());
        
        Contract contract = gson.fromJson(getResponse.body(), Contract.class);
        return contract;
    }


    public static ContractProposalResponse postContractProposal(BearerToken token, ContractProposal contract) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get post contract proposal";
            logger.error(message);
            throw new APIException(message);
        }

        String contractJson = gson.toJson(contract);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(BodyPublishers.ofString(contractJson))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating contract proposal post request";
            logger.error(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending contract proposal post request";
            logger.error(message, e);
            throw new APIException(message, e);
        }

        logger.info("Post Contract Proposal: Status Code {}", postResponse.statusCode());

        ContractProposalResponse response = gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        return response;
    }
    
    public static ContractProposalResponse cancelContractProposal(BearerToken token, String contractId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get cancel contract proposal";
            logger.error(message);
            throw new APIException(message);
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending cancel contract proposal post request for contract " + contractId;
            logger.error(message, e);
            throw new APIException(message, e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending cancel contract proposal post request for contract " + contractId;
            logger.error(message, e);
            throw new APIException(message, e);
        }

        logger.info("Cancel Contract Proposal: Status Code {}", postResponse.statusCode());        
        
        ContractProposalResponse response =  gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        return response;       
    }
    
    public static ContractProposalResponse acceptContractProposal(BearerToken token, String contractId, AcceptSettlement settlement) throws APIException {
        if (token == null){
            String message = "Token is null, unable to accept contract proposal";
            logger.error(message);
            throw new APIException(message);
        }        
        
        String settlementJson = gson.toJson(settlement);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(BodyPublishers.ofString(settlementJson))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating accept contract proposal post request for contract " + contractId;
            logger.error(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending accept contract proposal post request for contract " + contractId;
            logger.error(message, e);
            throw new APIException(message, e);
        }

        logger.info("Accept Contract Proposal: Status Code {}", postResponse.statusCode());

        ContractProposalResponse response =  gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        return response;
    }

    public static ContractProposalResponse declineContractProposal(BearerToken token, String contractId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to decline contract proposal";
            logger.error(message);
            throw new APIException(message);
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/decline"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating decline contract proposal post request for contract " + contractId;
            logger.error(message, e);
            throw new APIException(message, e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending decline contract proposal post request for contract " + contractId;
            logger.error(message, e);
            throw new APIException(message, e);
        }

        logger.info("Decline Contract Proposal: Status Code {}", postResponse.statusCode());

        ContractProposalResponse response =  gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        return response;
    }  

}