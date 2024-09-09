package com.equilend.simulator.api;

import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.agreement.Agreement;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.buyin.BuyinCompleteRequest;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.loan.LoanProposal;
import com.equilend.simulator.model.loan.LoanProposalApproval;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.instrument.Instrument;
import com.equilend.simulator.model.recall.Recall;
import com.equilend.simulator.model.recall.RecallProposal;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.rerate.RerateProposal;
import com.equilend.simulator.model.returns.ModelReturn;
import com.equilend.simulator.model.returns.ReturnAcknowledgement;
import com.equilend.simulator.model.returns.ReturnProposal;
import com.equilend.simulator.model.settlement.SettlementStatus;
import com.equilend.simulator.model.settlement.SettlementStatusUpdate;
import com.equilend.simulator.model.split.LoanSplit;
import com.equilend.simulator.model.split.LoanSplitLot;
import com.equilend.simulator.model.split.LoanSplitLotAppoval;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class APIConnector {

    private static final Logger logger = LogManager.getLogger(APIConnector.class.getName());
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class,
            (JsonDeserializer<OffsetDateTime>) (json, typeOfT, context) -> OffsetDateTime.parse(json.getAsString()))
        .registerTypeAdapter(OffsetDateTime.class,
            (JsonSerializer<OffsetDateTime>) (offsetDateTime, type, jsonSerializationContext) -> new JsonPrimitive(
                offsetDateTime.format(formatter))).registerTypeAdapter(LocalDate.class,
            (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
        .registerTypeAdapter(LocalDate.class,
            (JsonSerializer<LocalDate>) (localDate, type, jsonSerializationContext) -> new JsonPrimitive(
                localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))).create();
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

    public static List<Event> getAllEvents(OneSourceToken token, OffsetDateTime since, Long eventId)
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

    public static List<Loan> getAllLoans(OneSourceToken token, String status, String since)
        throws APIException {
        validateAPISetting(token);

        StringBuilder uri = new StringBuilder(restAPIURL);
        uri.append("/loans?size=2147483647&loanStatus=");
        uri.append(status);
        uri.append("&since=");
        uri.append(since);
        uri.append("T00:00:00Z");
        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(uri.toString()))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
        } catch (URISyntaxException e) {
            String message = "Error with creating loans get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending loans get request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get All Loans: Status Code {}", getResponse.statusCode());

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Type loanListType = new TypeToken<ArrayList<Loan>>() {
        }.getType();
        return gson.fromJson(getResponse.body(), loanListType);
    }

    public static Loan getLoanById(OneSourceToken token, String id) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans/" + id))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
        } catch (URISyntaxException e) {
            String message = "Error creating get request for loan " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> getResponse;
        try {
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error creating get request for loan " + id;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        logger.debug("Get Loan By Id: Status Code {}", getResponse.statusCode());

        if (getResponse.statusCode() / 100 != 2) {
            throw new APIException(getResponse.body());
        }

        Loan loan = gson.fromJson(getResponse.body(), Loan.class);
        return loan;
    }

    public static int postLoanProposal(OneSourceToken token, LoanProposal loan) throws APIException {
        validateAPISetting(token);

        String loanJson = gson.toJson(loan);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(loanJson))
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating loan proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending loan proposal post request";
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        Instrument instrument = loan.getTrade().getInstrument();
        String identifier = (instrument.getTicker() == null) ? instrument.getFigi() : instrument.getTicker();
        if (postResponse.statusCode() == 201) {
            LoanProposalResponse response = gson.fromJson(postResponse.body(), LoanProposalResponse.class);
            logger.info("Propose Loan {} with {} shares of {}", response.getLoanId(),
                loan.getTrade().getQuantity(), identifier);
        } else {
            logger.trace("Propose Loan with {} shares of {}: Status Code = {}", loan.getTrade().getQuantity(),
                identifier, postResponse.statusCode());
            logger.trace("POST response body: {}", postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int cancelLoan(OneSourceToken token, String loanId) throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans/" + loanId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending cancel loan proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending cancel loan proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200) {
            logger.info("Cancel Loan {}", loanId);
        } else {
            logger.trace("Cancel Loan {}: Status Code = {}", loanId, postResponse.statusCode());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int acceptLoan(OneSourceToken token, String loanId,
        LoanProposalApproval loanProposalApproval) throws APIException {
        validateAPISetting(token);

        String settlementJson = gson.toJson(loanProposalApproval);
        logger.trace(settlementJson);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans/" + loanId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(settlementJson)).build();
        } catch (URISyntaxException e) {
            String message = "Error with creating accept loan proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending accept loan proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200) {
            logger.info("Accept Loan {}", loanId);
        } else {
            logger.trace("Accept Loan {}: Status Code = {}", loanId, postResponse.statusCode());
            logger.trace(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int declineLoan(OneSourceToken token, String loanId) throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans/" + loanId + "/decline"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with creating decline loan proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending decline loan proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        if (postResponse.statusCode() == 200) {
            logger.info("Decline Loan {}", loanId);
        } else {
            logger.trace("Decline Loan {}: Status Code = {}", loanId, postResponse.statusCode());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int cancelPendingLoan(OneSourceToken token, String loanId) throws APIException {
        validateAPISetting(token);
        HttpResponse<String> postResponse;
        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/cancelpending"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending cancel pending loan post request for loanId " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static int instructLoanSettlementStatus(OneSourceToken token, String loanId,
        SettlementStatus status) throws APIException {
        validateAPISetting(token);

        SettlementStatusUpdate settlementStatusUpdate = new SettlementStatusUpdate().settlementStatus(
            status);
        HttpResponse<String> patchResponse;
        try {
            String body = gson.toJson(settlementStatusUpdate);
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
            patchResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending settlement loan status update post request for loanId " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(patchResponse);

        return patchResponse.statusCode();
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

    public static List<Rerate> getAllReratesOnLoan(OneSourceToken token, String loanId) throws APIException {
        validateAPISetting(token);

        HttpRequest getRequest;
        try {
            getRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans/" + loanId + "/rerates"))
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

    public static int postRerateProposal(OneSourceToken token, String loanId, RerateProposal rerate)
        throws APIException {
        validateAPISetting(token);

        String rerateJson = gson.toJson(rerate);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans/" + loanId + "/rerates"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
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

        if (postResponse.statusCode() == 201) {
            logger.debug("Rerate proposal posted successfully");
        } else {
            logger.debug("Error posting rerate proposal");
            logger.debug(postResponse.body());
            throw new APIException(postResponse.body());
        }
        return postResponse.statusCode();
    }

    public static int cancelRerateProposal(OneSourceToken token, String loanId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/rerates/" + rerateId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending cancel rerate proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending cancel rerate proposal post request for loan " + loanId;
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

    public static int cancelReratePending(OneSourceToken token, String loanId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/rerates/" + rerateId + "/cancelpending"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending cancel rerate pending post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending cancel rerate pending post request for loan " + loanId;
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

    public static int approveRerateProposal(OneSourceToken token, String loanId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/rerates/" + rerateId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending approve rerate proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending approve rerate proposal post request for loan " + loanId;
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

    public static int declineRerateProposal(OneSourceToken token, String loanId, String rerateId)
        throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/rerates/" + rerateId + "/decline"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e) {
            String message = "Error with sending decline rerate proposal post request for loan " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        HttpResponse<String> postResponse;
        try {
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String message = "Error with sending decline rerate proposal post request for loan " + loanId;
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

    public static int postReturnAck(OneSourceToken token, String loanId, String returnId,
        ReturnAcknowledgement returnAcknowledgement) throws APIException {
        validateAPISetting(token);

        String returnAcknowledgementJson = gson.toJson(returnAcknowledgement);
        logger.trace(returnAcknowledgementJson);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/returns/" + returnId + "/acknowledge"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
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

    public static int cancelReturn(OneSourceToken token, String loanId, String returnId) throws APIException {
        validateAPISetting(token);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/returns/" + returnId + "/cancel"))
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

    public static int proposeReturn(OneSourceToken token, String loanId, ReturnProposal returnProposal)
        throws APIException {
        validateAPISetting(token);

        String returnJson = gson.toJson(returnProposal);

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest.newBuilder().uri(new URI(restAPIURL + "/loans/" + loanId + "/returns"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(returnJson))
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

    public static int instructReturnSettlementStatus(OneSourceToken token, String loanId, String returnId,
        SettlementStatus status) throws APIException {
        validateAPISetting(token);

        SettlementStatusUpdate settlementStatusUpdate = new SettlementStatusUpdate().settlementStatus(
            status);

        HttpRequest postRequest;
        try {
            String body = gson.toJson(settlementStatusUpdate);
            postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/returns/" + returnId))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
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

    public static int acceptBuyin(OneSourceToken token, String loanId, String buyinId) throws APIException {
        validateAPISetting(token);

        HttpResponse<String> postResponse;
        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/buyins/completes/" + buyinId + "/accept"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message =
                "Error with sending approve buyin post request for loan " + loanId + " buyin " + buyinId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static int proposeBuyin(OneSourceToken token, String loanId, BuyinCompleteRequest buyinCompleteRequest)
        throws APIException {
        validateAPISetting(token);

        HttpResponse<String> postResponse;
        try {
            String body = gson.toJson(buyinCompleteRequest);
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/buyins/completes"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message =
                "Error with sending propose buyin post request for loan " + loanId;
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
            String message = "Error with sending recall get request for recallId " + recallId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(getResponse);

        return gson.fromJson(getResponse.body(), Recall.class);
    }

    public static int proposeRecall(OneSourceToken token, String loanId, RecallProposal recallProposal)
        throws APIException {
        validateAPISetting(token);
        HttpResponse<String> postResponse;
        try {
            String body = gson.toJson(recallProposal);
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/recalls"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending recall post request for loanId " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static int cancelRecall(OneSourceToken token, String loanId, String recallId) throws APIException {
        validateAPISetting(token);
        HttpResponse<String> postResponse;
        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/recalls/" + recallId + "/cancel"))
                .header("Authorization", "Bearer " + token.getAccessToken()).POST(HttpRequest.BodyPublishers.noBody())
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending cancel recall post request for recallId " + recallId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static LoanSplit getSplit(OneSourceToken token, String loanId, String splitId)
        throws APIException {
        HttpResponse<String> getResponse;
        try {
            HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/split/" + splitId))
                .header("Authorization", "Bearer " + token.getAccessToken()).build();
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending split get request for splitId " + splitId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(getResponse);

        return gson.fromJson(getResponse.body(), LoanSplit.class);
    }

    public static int approveSplit(OneSourceToken token, String loanId, String splitId, List<LoanSplitLotAppoval> splitLotAppovals) throws APIException {
        validateAPISetting(token);
        HttpResponse<String> postResponse;
        try {
            String body = gson.toJson(splitLotAppovals);
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/split/" + splitId + "/approve"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending approve split post request for splitId " + splitId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
    }

    public static int proposeSplit(OneSourceToken token, String loanId, List<LoanSplitLot> splitLots)
        throws APIException {
        validateAPISetting(token);
        HttpResponse<String> postResponse;
        try {
            String body = gson.toJson(splitLots);
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(restAPIURL + "/loans/" + loanId + "/split"))
                .header("Authorization", "Bearer " + token.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            String message = "Error with sending split post request for loanId " + loanId;
            logger.debug(message, e);
            throw new APIException(message, e);
        }

        isSuccess(postResponse);

        return postResponse.statusCode();
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