package com.equilend.simulator.configurator.rules;

import java.util.Map;
import java.util.Map.Entry;

public class AuthorizationRules implements Rules{

    private Map<String,String> oneSource;
    private Map<String,String> datalend;

    public AuthorizationRules(Map<String, Map<String, String>> rulesMap){
        oneSource = rulesMap.get("1Source");
        for (Entry<String, String> entry : oneSource.entrySet()){
            String value = entry.getValue();
            entry.setValue(value.substring(1, value.length()-1));
        }
        datalend = rulesMap.get("datalend");
        for (Entry<String, String> entry : datalend.entrySet()){
            String value = entry.getValue();
            entry.setValue(value.substring(1, value.length()-1));
        }
    }

    public Map<String, String> getOneSource() {
        return oneSource;
    }

    public Map<String, String> getDatalend() {
        return datalend;
    }
    
}