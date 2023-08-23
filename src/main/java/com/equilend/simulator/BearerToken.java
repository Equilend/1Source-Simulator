package com.equilend.simulator;

import java.util.Map;

public class BearerToken {
    private String accessToken;
    private static Map<String, String> login = null;
    private static BearerToken token = null;

    private BearerToken() throws APIException{
        Token tokenResponse = APIConnector.getBearerToken(login);
        this.accessToken = tokenResponse.getAccess_token();
    }

    public String getAccessToken(){
        return this.accessToken;
    }

    public static void configureToken(Map<String, String> loginInfo){
        if (login == null)
            login = loginInfo;
    }

    // Must Configure Token before Getting
    public static synchronized BearerToken getToken() throws APIException{
        if (token == null){
            token = new BearerToken();
        }
        return token;
    }
    
    public static synchronized void refreshToken() throws APIException{
        token = new BearerToken();
    }
}
