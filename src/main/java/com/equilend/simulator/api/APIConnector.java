package com.equilend.simulator.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.auth.OneSourceToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.os.client.model.Agreement;
import com.os.client.model.BuyinComplete;
import com.os.client.model.BuyinCompleteRequest;
import com.os.client.model.Contract;
import com.os.client.model.ContractProposal;
import com.os.client.model.ContractProposalApproval;
import com.os.client.model.Event;
import com.os.client.model.Instrument;
import com.os.client.model.ModelReturn;
import com.os.client.model.Rate;
import com.os.client.model.Recall;
import com.os.client.model.Rerate;
import com.os.client.model.RerateProposal;
import com.os.client.model.ReturnAcknowledgement;
import com.os.client.model.ReturnProposal;
import com.os.client.model.SettlementStatus;
import com.os.client.model.SettlementStatusUpdate;

public class APIConnector {

    private static final Logger logger = LogManager.getLogger(APIConnector.class.getName());
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new GsonBuilder()
    		.registerTypeAdapter(Rate.class, new RateGsonAdapter())
			.registerTypeAdapter(LocalDate.class, new LocalDateTypeGsonAdapter())
			.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeGsonAdapter()).create();
    private static String restAPIURL = null;

    public static void setRestAPIURL(String url) {
        restAPIURL = url;
    }

    public static OffsetDateTime getCurrentTime() {
        return OffsetDateTime.now(ZoneId.of("UTC"));
    }

    public static String formatTime(OffsetDateTime time) {
        return time.format(formatter);
    }

    public static List<Event> getAllEvents(OneSourceToken token, OffsetDateTime since, BigInteger eventId)
        throws APIException {
        validateAPISetting(token);

        String sinceStr = formatTime(since);
        String encodedSince = URLEncoder.encode(sinceStr, StandardCharsets.UTF_8);

        HttpRequest getRequest;
        try {
            String eventQuery = eventId != null ? "&fromEventId=" + eventId : "";
            getRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/events?since=" + encodedSince + eventQuery))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
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

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Type eventListType = new TypeToken<ArrayList<Event>>() {
        }.getType();
        return gson.fromJson(getResponse.body(), eventListType);
    }

    public static Agreement getAgreementById(OneSourceToken token, String id) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/agreements/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
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

        if (getResponse.statusCode() == 200) {
            Agreement agreement = gson.fromJson(getResponse.body(), Agreement.class);
            Instrument instrument = agreement.getTrade().getInstrument();
            String identifier = (instrument.getTicker() == null) ? instrument.getFigi() : instrument.getTicker();
            logger.info("Trade Agreement {} with {} shares of {}", id, agreement.getTrade().getQuantity(), identifier);
            return agreement;
        } else {
            logger.debug("Get Agreement By Id: Status Code {}", getResponse.statusCode());
            throw new APIException(getResponse.body());
        }
    }

    public static List<Contract> getAllContracts(OneSourceToken token, String status, String since)
        throws APIException {
        validateAPISetting(token);

        StringBuilder uri = new StringBuilder(restAPIURL);
        uri.append("/contracts?size=2147483647&contractStatus=");
        uri.append(status);
        uri.append("&since=");
        uri.append(since);
        uri.append("T00:00:00Z");
        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(uri.toString()))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
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

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Type contractListType = new TypeToken<ArrayList<Contract>>() {
        }.getType();
        return gson.fromJson(getResponse.body(), contractListType);
    }

    public static Contract getContractById(OneSourceToken token, String id) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
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

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Contract contract = gson.fromJson(getResponse.body(), Contract.class);
        return contract;
    }

    public static int postContractProposal(OneSourceToken token, ContractProposal contract) throws APIException {
        validateAPISetting(token);

        String contractJson = gson.toJson(contract);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
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

        Instrument instrument = contract.getTrade().getInstrument();
        String identifier = (instrument.getTicker() == null) ? instrument.getFigi() : instrument.getTicker();
        if (postResponse.statusCode() == 201) {
            ContractProposalResponse response = gson.fromJson(postResponse.body(), ContractProposalResponse.class);
            logger.info("Propose Contract {} with {} shares of {}", response.getContractId(),
                contract.getTrade().getQuantity(), identifier);
        } else {
            logger.trace("Propose Contract with {} shares of {}: Status Code = {}", contract.getTrade().getQuantity(),
                identifier, postResponse.statusCode());
            logger.trace("POST response body: {}", postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int cancelContractProposal(OneSourceToken token, String contractId) throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts/" + contractId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
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

        if (postResponse.statusCode() == 200) {
            logger.info("Cancel Contract {}", contractId);
        } else {
            logger.trace("Cancel Contract {}: Status Code = {}", contractId, postResponse.statusCode());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int acceptContractProposal(OneSourceToken token, String contractId,
        ContractProposalApproval contractProposalApproval) throws APIException {
        validateAPISetting(token);

        String settlementJson = gson.toJson(contractProposalApproval);
        logger.trace(settlementJson);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts/" + contractId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(BodyPublishers.ofString(settlementJson)).build();
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

        if (postResponse.statusCode() == 200) {
            logger.info("Accept Contract {}", contractId);
        } else {
            logger.trace("Accept Contract {}: Status Code = {}", contractId, postResponse.statusCode());
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int declineContractProposal(OneSourceToken token, String contractId) throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts/" + contractId + "/decline"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
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

        if (postResponse.statusCode() == 200) {
            logger.info("Decline Contract {}", contractId);
        } else {
            logger.trace("Decline Contract {}: Status Code = {}", contractId, postResponse.statusCode());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static List<Rerate> getAllRerates(OneSourceToken token) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/rerates"))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
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

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Type rerateListType = new TypeToken<ArrayList<Rerate>>() {
        }.getType();
        return gson.fromJson(getResponse.body(), rerateListType);
    }

    public static List<Rerate> getAllReratesOnContract(OneSourceToken token, String contractId) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates"))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
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

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Type rerateListType = new TypeToken<ArrayList<Rerate>>() {
        }.getType();
        return gson.fromJson(getResponse.body(), rerateListType);
    }

    public static Rerate getRerateById(OneSourceToken token, String id) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/rerates/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
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

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Rerate rerate = gson.fromJson(getResponse.body(), Rerate.class);
        return rerate;
    }

    public static int postRerateProposal(OneSourceToken token, String contractId, RerateProposal rerate)
        throws APIException {
        validateAPISetting(token);

        String rerateJson = gson.toJson(rerate);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(BodyPublishers.ofString(rerateJson))
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

        if (postResponse.statusCode() == 201) {
            logger.info("Rerate proposal posted successfully");
        } else {
            logger.trace("Error posting rerate proposal");
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int cancelRerateProposal(OneSourceToken token, String contractId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates/" + rerateId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
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

        if (postResponse.statusCode() == 200) {
            logger.info("Rerate proposal cancelled successfully");
        } else {
            logger.trace("Error cancelling rerate proposal");
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int cancelReratePending(OneSourceToken token, String contractId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates/" + rerateId + "/cancelpending"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending cancel rerate pending post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending cancel rerate pending post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200) {
            logger.info("Rerate pending cancelled successfully");
        } else {
            logger.trace("Error cancelling rerate pending");
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int approveRerateProposal(OneSourceToken token, String contractId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates/" + rerateId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
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

        if (postResponse.statusCode() == 200) {
            logger.info("Rerate proposal approved successfully");
        } else {
            logger.trace("Error approving rerate proposal");
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int declineRerateProposal(OneSourceToken token, String contractId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/rerates/" + rerateId + "/decline"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
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

        if (postResponse.statusCode() == 200) {
            logger.info("Rerate proposal declined successfully");
        } else {
            logger.trace("Error declining rerate proposal");
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static ModelReturn getReturnById(OneSourceToken token, String id) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/returns/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
        } catch (URISyntaxException e) {
            String message = "Error creating get request for return " + id;
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

        logger.debug("Get Return By Id: Status Code {}", getResponse.statusCode());

        isSuccess(getResponse);

        ModelReturn oneSourceReturn = gson.fromJson(getResponse.body(), ModelReturn.class);
        return oneSourceReturn;
    }

    public static int postReturnAck(OneSourceToken token, String contractId, String returnId,
        ReturnAcknowledgement returnAcknowledgement) throws APIException {
        validateAPISetting(token);

        String returnAcknowledgementJson = gson.toJson(returnAcknowledgement);
        logger.trace(returnAcknowledgementJson);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/returns/" + returnId + "/acknowledge"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(returnAcknowledgementJson)).build();
        } catch (URISyntaxException e) {
            String message = "Error creating post Return Acknowledgement " + returnId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating post request for Return Acknowledgement " + returnId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Post Ack Return: Status Code {}", postResponse.statusCode());

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static int cancelReturn(OneSourceToken token, String contractId, String returnId) throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/returns/" + returnId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error creating post Cancel Return " + returnId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating post request for Cancel Return " + returnId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Cancel Return: Status Code {}", postResponse.statusCode());

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static int proposeReturn(OneSourceToken token, String contractId, ReturnProposal returnProposal)
        throws APIException {
        validateAPISetting(token);

        String returnJson = gson.toJson(returnProposal);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/contracts/" + contractId + "/returns"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(BodyPublishers.ofString(returnJson))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating return proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending return proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 201) {
            logger.info("Return proposal posted successfully");
        } else {
            logger.trace("Error posting return proposal");
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int instructReturnSettlementStatus(OneSourceToken token, String contractId, String returnId,
        SettlementStatus status) throws APIException {
        validateAPISetting(token);

        SettlementStatusUpdate settlementStatusUpdate = new SettlementStatusUpdate().settlementStatus(
            status);

        HttpRequest postRequest;
        try {
            String body = gson.toJson(settlementStatusUpdate);
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/returns/" + returnId))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error creating patch Return Settlement Status " + returnId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> patchResponse;
        try {
            patchResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating patch request for Return Settlement Status " + returnId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Update Return Settlement Status : Status Code {}", patchResponse.statusCode());

        isSuccess(patchResponse);

        return patchResponse.statusCode();
    }

    public static BuyinComplete getBuyinById(OneSourceToken token, String buyinId) throws APIException {
        validateAPISetting(token);

        HttpResponse<String> getResponse;
        try {
            HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/buyins/completes/" + buyinId))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending buyin get request for buyin " + buyinId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(getResponse);

        return gson.fromJson(getResponse.body(), BuyinComplete.class);
    }

    public static int acceptBuyin(OneSourceToken token, String contractId, String buyinId) throws APIException {
        validateAPISetting(token);

        HttpResponse<String> postResponse;
        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/buyins/completes/" + buyinId + "/accept"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message =
                "Error with sending approve buyin post request for contract " + contractId + " buyin " + buyinId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static int proposeBuyin(OneSourceToken token, String contractId, BuyinCompleteRequest buyinCompleteRequest) throws APIException {
        validateAPISetting(token);

        HttpResponse<String> postResponse;
        try {
            String body = gson.toJson(buyinCompleteRequest);
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/contracts/" + contractId + "/buyins/completes"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message =
                "Error with sending propose buyin post request for contract " + contractId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static Recall getRecallById(OneSourceToken token, String recallId) throws APIException {
        HttpResponse<String> getResponse;
        try {
            HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/recalls/" + recallId))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending recall get request for buyin " + recallId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(getResponse);

        return gson.fromJson(getResponse.body(), Recall.class);
    }

    private static void validateAPISetting(OneSourceToken token) throws APIException {
        if (token == null) {
            String message = "Token is null, unable to do request";
            logger.debug(message);
            throw new APIException(message);
        }

        if (restAPIURL == null) {
            throw new APIException("1Source REST API URL not properly loaded");
        }
    }

    private static void isSuccess(HttpResponse response) throws APIException {
        if (response.statusCode() / 100 != 2) {
            throw new APIException(String.valueOf(response.body()));
        }
    }
}