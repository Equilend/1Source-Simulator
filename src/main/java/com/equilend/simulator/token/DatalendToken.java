package com.equilend.simulator.token;

import java.util.Map;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.KeycloakConnector;

public class DatalendToken {
   
    private String accessToken;
    private static Map<String, String> login = null;
    private static String url = null;
    private static DatalendToken token = null;

    private DatalendToken() throws APIException {
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
    public static synchronized DatalendToken getToken() throws APIException {
        if (token == null){
            token = new DatalendToken();
        }
        return token;
    }
    
    public static synchronized void refreshToken() throws APIException {
        token = new DatalendToken();
    }

}