package com.equilend.simulator.api;

import com.equilend.simulator.auth.Token;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeycloakConnector {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
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

    public static Token getBearerToken(Map<String, String> loginInfo, String url) throws APIException {
        if (loginInfo == null) {
            throw new APIException("Login info not configured or failed to be read");
        }

        HttpRequest postRequest;
        try {
            postRequest = HttpRequest
                .newBuilder()
                .uri(new URI(url))
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
        if (token.getError() != null) {
            String message = "Error authorizing bearer token: " + token.getError_description();
            logger.debug(message);
            throw new APIException(message);
        }

        return token;
    }

}
