package com.equilend.simulator.token;

import java.util.Map;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.KeycloakConnector;

public class OneSourceToken {

    private String accessToken;
    private static Map<String, String> login = null;
    private static String url = null;
    private static OneSourceToken token = null;

    private OneSourceToken() throws APIException {
        Token tokenResponse = KeycloakConnector.getBearerToken(login, url);
        this.accessToken = tokenResponse.getAccess_token();
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
    public static synchronized OneSourceToken getToken() throws APIException {
        if (token == null){
            token = new OneSourceToken();
        }
        return token;
    }
    
    public static synchronized void refreshToken() throws APIException {
        token = new OneSourceToken();
    }
    
}