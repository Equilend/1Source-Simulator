package com.equilend.simulator;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.Agreement.Agreement;
import com.equilend.simulator.Settlement.AcceptSettlement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class APIConnector 
{
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new Gson();
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

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

    public static Token getBearerToken(Map<String, String> login_info) throws URISyntaxException, IOException, InterruptedException
    {
        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageauth.equilend.com/auth/realms/1Source/protocol/openid-connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(BodyPublishers.ofString(encodeMapAsString(login_info)))
            .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());

        return gson.fromJson(postResponse.body(), Token.class);
    }

    public static List<Agreement> getAllAgreements(Token token, String sinceTime ) throws URISyntaxException, IOException, InterruptedException
    {
        String time = URLEncoder.encode(sinceTime, java.nio.charset.StandardCharsets.UTF_8.toString());
        
        HttpRequest getRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/agreements" + "?" + "since=" + time))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .build();
        
        HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        
        Type agreementListType = new TypeToken<ArrayList<Agreement>>(){}.getType();
        return gson.fromJson(getResponse.body(), agreementListType);
    }   

    public static List<Agreement> getAllAgreementsToday(Token token) throws URISyntaxException, IOException, InterruptedException
    {
        OffsetDateTime currentDay = getCurrentTime().withHour(0).withMinute(0).withSecond(0);
        String currentDayTimeStr = formatTime(currentDay);
        return getAllAgreements(token, currentDayTimeStr);
    }

    public static Contract getContractById(Token token, String contractId) throws URISyntaxException, IOException, InterruptedException
    {
        HttpRequest getRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());

        Type contractListType = new TypeToken<ArrayList<Contract>>(){}.getType();
        List<Contract> list = gson.fromJson(getResponse.body(), contractListType);
        return list.get(0);
    }

    public static ContractProposalResponse postContractProposal(Token token, ContractProposal contract) throws URISyntaxException, IOException, InterruptedException
    {
        String contractJson = gson.toJson(contract);

        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts"))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .POST(BodyPublishers.ofString(contractJson))
            .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        return gson.fromJson(postResponse.body(), ContractProposalResponse.class);
    }

    public static ContractProposalResponse acceptContractProposal(Token token, String contractId, AcceptSettlement settlement) throws URISyntaxException, IOException, InterruptedException
    {
        String settlementJson = gson.toJson(settlement);
        // System.out.println(settlementJson);
        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/approve"))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .POST(BodyPublishers.ofString(settlementJson))
            .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());

        // System.out.println(postResponse.body());
        // System.out.println();
        return gson.fromJson(postResponse.body(), ContractProposalResponse.class);
    }
    
    public static ContractProposalResponse cancelContractProposal(Token token, String contractId) throws URISyntaxException, IOException, InterruptedException
    {
        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts/" + contractId + "/cancel"))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
            
        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());

        System.out.println(postResponse.body());
        System.out.println();            
        return gson.fromJson(postResponse.body(), ContractProposalResponse.class);        
    }

    // TODO: Decline Contract Proposal
}
