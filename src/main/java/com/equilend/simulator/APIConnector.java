package com.equilend.simulator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.Agreement.Agreement;
import com.equilend.simulator.Settlement.AcceptSettlement;
import com.equilend.simulator.Trade.TransactingParty.Party;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class APIConnector 
{
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new Gson();
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private static final Logger logger = LogManager.getLogger();

    public static OffsetDateTime getCurrentTime()
    {
        return OffsetDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS);
    }
    public static String formatTime(OffsetDateTime time)
    {
        return time.format(formatter);
    }

    public static String encodeMapAsString(Map<String, String> formData) 
    {
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
    public static Token getBearerToken(Map<String, String> loginInfo) throws APIException
    {
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

    public static ContractProposalResponse postContractProposal(Token token, ContractProposal contract) throws APIException
    {
        String contractJson = gson.toJson(contract);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts"))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .POST(BodyPublishers.ofString(contractJson))
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error with creating contract proposal post request", e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error with sending contract proposal post request", e);
        }
        ContractProposalResponse response = gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        System.out.println("Proposing contract: " + response.getContractId());
        return response;
    }
    public static ContractProposalResponse cancelContractProposal(Token token, String contractId) throws APIException
    {
        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error with sending cancel contract proposal post request", e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error with sending cancel contract proposal post request", e);
        }
        
        ContractProposalResponse response =  gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        System.out.println("Cancelling contract: " + response.getContractId());
        return response;       
    }
    public static ContractProposalResponse acceptContractProposal(Token token, String contractId, AcceptSettlement settlement) throws APIException
    {
        String settlementJson = gson.toJson(settlement);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .POST(BodyPublishers.ofString(settlementJson))
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error with creating accept contract proposal post request", e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error with sending accept contract proposal post request", e);
        }

        ContractProposalResponse response =  gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        System.out.println("Accepting contract: " + response.getContractId());
        return response;
    }
    public static ContractProposalResponse declineContractProposal(Token token, String contractId) throws APIException
    {
        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/decline"))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error with creating decline contract proposal post request", e);
        }
            
        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error with sending decline contract proposal post request", e);
        }

        ContractProposalResponse response =  gson.fromJson(postResponse.body(), ContractProposalResponse.class);
        System.out.println("Declining contract: " + response.getContractId());
        return response;
    }  

    private static String encode(String str){
        String encoded = "";
        try {
            encoded = URLEncoder.encode(str, java.nio.charset.StandardCharsets.UTF_8.toString());
        }
        catch (UnsupportedEncodingException e){
            throw new AssertionError(e);
        }
        return encoded;
    }

    public static List<Agreement> getAllAgreements(Token token, OffsetDateTime since, OffsetDateTime before) throws APIException
    {
        if (token == null) logger.info("huh");
        String sinceStr = formatTime(since);
        String encodedSince = encode(sinceStr);
        String beforeStr = formatTime(before);
        String encodedBefore = encode(beforeStr);
        
        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/agreements" + "?" + "since=" + encodedSince + "&" + "before=" + encodedBefore))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error with creating agreements get request", e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error with sending agreements get request", e);
        }    
        Type agreementListType = new TypeToken<ArrayList<Agreement>>(){}.getType();
        return gson.fromJson(getResponse.body(), agreementListType);
    }
    public static List<Contract> getAllContracts(Token token, OffsetDateTime since, OffsetDateTime before) throws APIException
    {
        String sinceStr = formatTime(since);
        String encodedSince = encode (sinceStr);
        String beforeStr = formatTime(before);
        String encodedBefore = encode(beforeStr);
        
        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts" + "?" + "since=" + encodedSince + "&" + "before=" + encodedBefore))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error with creating contracts get request", e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error with sending contracts get request", e);
        }
        Type contractListType = new TypeToken<ArrayList<Contract>>(){}.getType();
        return gson.fromJson(getResponse.body(), contractListType);
    }    
    public static List<Party> getAllParties (Token token) throws APIException
    {
        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/parties"))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error creating parties get request", e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error sending parties get request", e);
        }

        Type partyListType = new TypeToken<ArrayList<Party>>(){}.getType();
        return gson.fromJson(getResponse.body(), partyListType);
    }

    public static Party getPartyById (Token token, String id) throws APIException
    {
        HttpRequest getRequest;
        try {
            getRequest = HttpRequest
                .newBuilder()
                .uri(new URI("https://stageapi.equilend.com/v1/ledger/parties" + "/" + id))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .build();
        } catch (URISyntaxException e) {
            throw new APIException("Error creating party by id get request", e);
        }
        
        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new APIException("Error sending party by id get request", e);
        }
        
        Type partyListType = new TypeToken<ArrayList<Party>>(){}.getType();
        ArrayList<Party> partyList = gson.fromJson(getResponse.body(), partyListType);
        return partyList.get(0);
    }    

}
