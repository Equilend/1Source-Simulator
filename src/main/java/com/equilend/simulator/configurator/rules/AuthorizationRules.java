package com.equilend.simulator.configurator.rules;

import java.util.Map;

public class AuthorizationRules implements Rules{

    private Map<String,String> oneSource;
    private Map<String,String> datalend;

    public AuthorizationRules(Map<String, Map<String, String>> rulesMap){
        oneSource = rulesMap.get("1source");
        datalend = rulesMap.get("datalend");
    }

    public Map<String, String> getOneSource() {
        return oneSource;
    }

    public Map<String, String> getDatalend() {
        return datalend;
    }
    
}