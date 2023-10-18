package com.equilend.simulator.configurator.rules;

import java.util.Map;

public class GeneralRules implements Rules{
    
    private String userPartyId; 
    private String botPartyId; 
    private int maxRefreshAttempts;
    private int eventFetchIntervalMillis;
    private String oneSourceKeycloakURL;
    private String oneSourceAPIURL;
    private String datalendKeycloakURL;
    private String datalendAPIURL;

    public GeneralRules(Map<String, Map<String, String>> rulesMap){
        Map<String, String> general = rulesMap.get("general");

        this.userPartyId = cleanString(general.get("user_party_id"));
        this.botPartyId = cleanString(general.get("bot_party_id"));
        this.maxRefreshAttempts = Integer.parseInt(general.get("max_refresh_attempts"));
        this.eventFetchIntervalMillis = 1000 * Integer.parseInt(general.get("event_fetch_interval_secs"));
        this.oneSourceKeycloakURL = cleanString(general.get("1source_keycloak_url"));
        this.oneSourceAPIURL = cleanString(general.get("1source_api_url"));
        this.datalendKeycloakURL = cleanString(general.get("datalend_keycloak_url"));
        this.datalendAPIURL = cleanString(general.get("datalend_api_url"));      
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

    public String getOneSourceKeycloakURL() {
        return oneSourceKeycloakURL;
    }

    public String getOneSourceAPIURL() {
        return oneSourceAPIURL;
    }

    public String getDatalendKeycloakURL() {
        return datalendKeycloakURL;
    }

    public String getDatalendAPIURL() {
        return datalendAPIURL;
    }
    
}
