package com.equilend.simulator.auth;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.KeycloakConnector;

public class DatalendToken {
    private static final Logger logger = LogManager.getLogger(DatalendToken.class.getName());

    private final String accessToken;
    private static Map<String, String> login = null;
    private static String url = null;
    private static DatalendToken token = null;

    private DatalendToken() throws APIException {
        try {
            Token tokenResponse = KeycloakConnector.getBearerToken(login, url);
            this.accessToken = tokenResponse.getAccess_token();
            logger.debug("Datalend auth token: " + accessToken);
        } catch (APIException e) {
            logger.error("Error retrieving Datalend auth token");
            throw new RuntimeException(e);
        }
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public static void configureToken(Map<String, String> keycloakLoginInfo, String keycloakUrl) {
        if (login == null || url == null) {
            login = keycloakLoginInfo;
            url = keycloakUrl;
        }
    }

    // Must Configure Token before Getting
    public static synchronized DatalendToken getToken() throws APIException {
        if (token == null) {
            token = new DatalendToken();
        }
        return token;
    }

    public static synchronized void refreshToken() throws APIException {
        token = new DatalendToken();
    }

}