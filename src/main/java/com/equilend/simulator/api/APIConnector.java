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

import com.equilend.simulator.model.agreement.Agreement;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.contract.ContractProposal;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.rerate.RerateProposal;
import com.equilend.simulator.model.settlement.AcceptSettlement;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.token.Token;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class APIConnector {

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new Gson();
    private static String keycloakURL = null;
    private static String restAPIURL = null;
    private static final Logger logger = LogManager.getLogger();

    public static void setKeycloakURL(String url){
        keycloakURL = url;
    }

    public static void setRestAPIURL(String url){
        restAPIURL = url;
    }

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

        if (keycloakURL == null){
            throw new APIException("Keycloak API URL not properly loaded");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(keycloakURL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString(encodeMapAsString(loginInfo)))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating token post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending token post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        Token token = gson.fromJson(postResponse.body(), Token.class);
        if (token.getError() != null){ 
            String message = "Error authorizing bearer token: " + token.getError_description();
            logger.debug(message);
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
            logger.debug(message);
            throw new APIException(message);
        }

        String sinceStr = formatTime(since);
        String encodedSince = URLEncoder.encode(sinceStr, StandardCharsets.UTF_8);

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }        

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/events?since=" + encodedSince + "&fromEventId=" + eventId))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating events get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending events get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get All Events: Status Code {}", getResponse.statusCode());

        Type eventListType = new TypeToken<ArrayList<Event>>(){}.getType();
        return gson.fromJson(getResponse.body(), eventListType);
    }     

    public static Agreement getAgreementById(BearerToken token, String id) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get agreement by id";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        } 

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/agreements/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error creating get request for agreement " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating get request for agreement " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        Agreement agreement = gson.fromJson(getResponse.body(), Agreement.class);
        if (getResponse.statusCode() == 200){
            logger.info("Trade Agreement {} with {} shares of {}", 
            id, agreement.getTrade().getQuantity(), agreement.getTrade().getInstrument().getTicker());
        }
        else{
            logger.debug("Get Agreement By Id: Status Code {}", getResponse.statusCode());
        }
        return agreement;
    }

    public static List<Contract> getAllContracts(BearerToken token, String status) throws APIException{
        if (token == null){
            String message = "Token is null, unable to get all events";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }        

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts?size=2147483647&contractStatus=" + status))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating contracts get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending contracts get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get All Contracts: Status Code {}", getResponse.statusCode());

        Type contractListType = new TypeToken<ArrayList<Contract>>(){}.getType();
        return gson.fromJson(getResponse.body(), contractListType);
    }

    public static Contract getContractById(BearerToken token, String id) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get contract by id";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        } 

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error creating get request for contract " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating get request for contract " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
       
        logger.debug("Get Contract By Id: Status Code {}", getResponse.statusCode());
        
        Contract contract = gson.fromJson(getResponse.body(), Contract.class);
        return contract;
    }

    public static int postContractProposal(BearerToken token, ContractProposal contract) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get post contract proposal";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        } 

        String contractJson = gson.toJson(contract);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(BodyPublishers.ofString(contractJson))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating contract proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending contract proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        ContractProposalResponse response = gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        if (postResponse.statusCode() == 201){
            logger.info("Propose Contract {} with {} shares of {}", 
            response.getContractId(), contract.getTrade().getQuantity(), contract.getTrade().getInstrument().getTicker());
        }
        else{
            logger.trace("Propose Contract with {} shares of {}: Status Code = {}", contract.getTrade().getQuantity(), contract.getTrade().getInstrument().getTicker(), postResponse.statusCode());
            logger.trace("POST response body: {}", postResponse.body());
        }
        return postResponse.statusCode();
    }
    
    public static int cancelContractProposal(BearerToken token, String contractId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get cancel contract proposal";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending cancel contract proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending cancel contract proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200){
            logger.info("Cancel Contract {}", contractId);
        }
        else{
            logger.trace("Cancel Contract {}: Status Code = {}", contractId, postResponse.statusCode());
        }
        return postResponse.statusCode();       
    }
    
    public static int acceptContractProposal(BearerToken token, String contractId, AcceptSettlement settlement) throws APIException {
        if (token == null){
            String message = "Token is null, unable to accept contract proposal";
            logger.debug(message);
            throw new APIException(message);
        }       
         
        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }         

        String settlementJson = gson.toJson(settlement);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(BodyPublishers.ofString(settlementJson))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating accept contract proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending accept contract proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200){
            logger.info("Accept Contract {}", contractId);
        }
        else{
            logger.trace("Accept Contract {}: Status Code = {}", contractId, postResponse.statusCode());
        }
        return postResponse.statusCode();
    }

    public static int declineContractProposal(BearerToken token, String contractId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to decline contract proposal";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/decline"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating decline contract proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending decline contract proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200){
            logger.info("Decline Contract {}", contractId);
        }
        else{
            logger.trace("Decline Contract {}: Status Code = {}", contractId, postResponse.statusCode());
        }
        return postResponse.statusCode();
    }  

    public static List<Rerate> getAllRerates(BearerToken token) throws APIException{
        if (token == null){
            String message = "Token is null, unable to get all events";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }        

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/rerates"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating rerates get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending rerates get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get All Rerates: Status Code {}", getResponse.statusCode());

        Type rerateListType = new TypeToken<ArrayList<Rerate>>(){}.getType();
        return gson.fromJson(getResponse.body(), rerateListType);
    }

    public static List<Rerate> getAllReratesOnContract(BearerToken token, String contractId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get all events";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }        

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId +"/rerates"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating rerates get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending rerates get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get All Rerates: Status Code {}", getResponse.statusCode());

        Type rerateListType = new TypeToken<ArrayList<Rerate>>(){}.getType();
        return gson.fromJson(getResponse.body(), rerateListType);
    }

    public static Rerate getRerateById(BearerToken token, String id) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get rerate by id";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        } 

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/rerates/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error creating get request for rerate " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating get request for rerate " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
       
        logger.debug("Get Rerate By Id: Status Code {}", getResponse.statusCode());
        
        Rerate rerate = gson.fromJson(getResponse.body(), Rerate.class);
        return rerate;
    }

    public static int postRerateProposal(BearerToken token, String contractId, RerateProposal rerate) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get post contract proposal";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        } 

        String rerateJson = gson.toJson(rerate);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(BodyPublishers.ofString(rerateJson))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating rerate proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending rerate proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 201){
            logger.info("Rerate proposal posted successfully");
        }
        else{
            logger.trace("Error posting rerate proposal");
            logger.trace(postResponse.body());
        }
        return postResponse.statusCode();
    }
    
    public static int cancelRerateProposal(BearerToken token, String contractId, String rerateId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get cancel rerate proposal";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates/" + rerateId +"/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending cancel rerate proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending cancel rerate proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200){
            logger.info("Rerate proposal cancelled successfully");
        }
        else{
            logger.trace("Error cancelling rerate proposal");
            logger.trace(postResponse.body());
        }
        return postResponse.statusCode();
    }
    
    public static int approveRerateProposal(BearerToken token, String contractId, String rerateId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get approve rerate proposal";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates/" + rerateId +"/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending approve rerate proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending approve rerate proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200){
            logger.info("Rerate proposal approved successfully");
        }
        else{
            logger.trace("Error approving rerate proposal");
            logger.trace(postResponse.body());
        }
        return postResponse.statusCode();
    }
    
    public static int declineRerateProposal(BearerToken token, String contractId, String rerateId) throws APIException {
        if (token == null){
            String message = "Token is null, unable to get decline rerate proposal";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null){
            throw new APIException("1Source REST API URL not properly loaded");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates/" + rerateId +"/decline"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending decline rerate proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending decline rerate proposal post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200){
            logger.info("Rerate proposal declined successfully");
        }
        else{
            logger.trace("Error declining rerate proposal");
            logger.trace(postResponse.body());
        }
        return postResponse.statusCode();
    }

}