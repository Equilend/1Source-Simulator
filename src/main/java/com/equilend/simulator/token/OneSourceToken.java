package com.equilend.simulator.token;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.KeycloakConnector;

public class OneSourceToken {

    private volatile String accessToken;
    private static Map<String, String> login = null;
    private static String url = null;
    private static OneSourceToken token = null;
    private static final Logger logger = LogManager.getLogger();

    private OneSourceToken() throws APIException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable () {
            public void run() {
                Token tokenResponse;
                try {
                    tokenResponse = KeycloakConnector.getBearerToken(login, url);
                    accessToken = tokenResponse.getAccess_token();                
                } catch (APIException e) {
                    logger.error("Error retrieving 1Source auth token");
                }
            }
        }, 0, 840, TimeUnit.SECONDS);
    }

    public String getAccessToken(){
        return this.accessToken;
    }

    public static void configureToken(Map<String, String> authRules, String url_) {
        if (login == null || url == null){
            login = authRules;
            url = url_;
        }
    }

    // Must Configure Token before Getting
    public static OneSourceToken getToken() throws APIException {
        if (token == null){
            token = new OneSourceToken();
        }
        return token;
    }
    
}