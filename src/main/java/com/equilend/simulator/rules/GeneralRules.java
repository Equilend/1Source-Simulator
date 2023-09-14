package com.equilend.simulator.rules;

import java.util.Map;

public class GeneralRules implements Rules{
    
    private String userPartyId; 
    private int maxRefreshAttempts;
    private int eventFetchIntervalSecs;

    public GeneralRules(Map<String, Map<String, String>> rulesMap){
        Map<String, String> general = rulesMap.get("general");

        this.userPartyId = general.get("user_party_id");
        this.maxRefreshAttempts = Integer.parseInt(general.get("max_refresh_attempts"));
        this.eventFetchIntervalSecs = Integer.parseInt(general.get("event_fetch_interval_secs"));
    }

    public String getUserPartyId() {
        return userPartyId;
    }

    public void setUserPartyId(String userPartyId) {
        this.userPartyId = userPartyId;
    }

    public int getMaxRefreshAttempts() {
        return maxRefreshAttempts;
    }

    public void setMaxRefreshAttempts(int maxRefreshAttempts) {
        this.maxRefreshAttempts = maxRefreshAttempts;
    }

    public int getEventFetchIntervalSecs() {
        return eventFetchIntervalSecs;
    }

    public void setEventFetchIntervalSecs(int eventFetchIntervalSecs) {
        this.eventFetchIntervalSecs = eventFetchIntervalSecs;
    }

    
}
