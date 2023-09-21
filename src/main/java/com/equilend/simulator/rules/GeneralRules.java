package com.equilend.simulator.rules;

import java.util.Map;

public class GeneralRules implements Rules{
    
    private String userPartyId; 
    private String botPartyId; 
    private int maxRefreshAttempts;
    private int eventFetchIntervalMillis;

    public GeneralRules(Map<String, Map<String, String>> rulesMap){
        Map<String, String> general = rulesMap.get("general");

        this.userPartyId = cleanString(general.get("user_party_id"));
        this.botPartyId = cleanString(general.get("bot_party_id"));
        this.maxRefreshAttempts = Integer.parseInt(general.get("max_refresh_attempts"));
        this.eventFetchIntervalMillis = 1000 * Integer.parseInt(general.get("event_fetch_interval_secs"));
    }

    private String cleanString(String raw){
        return raw.substring(1, raw.length()-1);
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
    
}
