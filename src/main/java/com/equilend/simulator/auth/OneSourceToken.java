package com.equilend.simulator.auth;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.KeycloakConnector;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OneSourceToken {

    private static final Logger logger = LogManager.getLogger(OneSourceToken.class.getName());
    private volatile String accessToken;
    private static Map<String, String> loginInfo = null;
    private static String url = null;
    private static OneSourceToken token = null;

    private OneSourceToken() throws APIException {
        Token tokenResponse;
        try {
            tokenResponse = KeycloakConnector.getBearerToken(loginInfo, url);
            accessToken = tokenResponse.getAccess_token();
        } catch (APIException e) {
            logger.error("Error retrieving 1Source auth token");
            throw new RuntimeException(e);
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Token tokenResponse;
                try {
                    tokenResponse = KeycloakConnector.getBearerToken(loginInfo, url);
                    accessToken = tokenResponse.getAccess_token();
                } catch (APIException e) {
                    logger.error("Error retrieving 1Source auth token");
                }
            }
        }, 600, 600, TimeUnit.SECONDS);
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public static void configureToken(Map<String, String> keycloakLoginInfo, String keycloakUrl) {
        if (loginInfo == null || url == null) {
            loginInfo = keycloakLoginInfo;
            url = keycloakUrl;
        }
    }

    // Must Configure Token before Getting
    public static OneSourceToken getToken() throws APIException {
        if (token == null) {
            token = new OneSourceToken();
        }
        return token;
    }

}