package com.equilend.simulator.rules;

import java.util.Map;

public class GeneralRules implements Rules{
    
    private String userPartyId; 
    private String botPartyId; 
    private int maxRefreshAttempts;
    private int eventFetchIntervalMillis;
    private String keycloakURL;
    private String restAPIURL;

    public GeneralRules(Map<String, Map<String, String>> rulesMap){
        Map<String, String> general = rulesMap.get("general");

        this.userPartyId = cleanString(general.get("user_party_id"));
        this.botPartyId = cleanString(general.get("bot_party_id"));
        this.maxRefreshAttempts = Integer.parseInt(general.get("max_refresh_attempts"));
        this.eventFetchIntervalMillis = 1000 * Integer.parseInt(general.get("event_fetch_interval_secs"));
        this.keycloakURL = cleanString(general.get("keycloak_url"));
        this.restAPIURL = cleanString(general.get("rest_api_url"));
    }

    private String cleanString(String raw){
        return raw.replace("\"", "");
    }
    
    public String getUserPartyId() {
        return userPartyId;
    }
        
    public String getBotPartyId() {
        return botPartyId;
    }

    public int getMaxRefreshAttempts() {
        return maxRefreshAttempts;
    }

    public int getEventFetchIntervalMillis() {
        return eventFetchIntervalMillis;
    }

    public String getKeycloakURL() {
        return keycloakURL;
    }

    public String getRestAPIURL() {
        return restAPIURL;
    }
    
}
