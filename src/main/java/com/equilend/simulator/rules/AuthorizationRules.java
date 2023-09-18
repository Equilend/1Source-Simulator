package com.equilend.simulator.rules;

import java.util.Map;
import java.util.Map.Entry;

public class AuthorizationRules implements Rules{

    private Map<String,String> borrower;
    private Map<String,String> lender;

    public AuthorizationRules(Map<String, Map<String, String>> rulesMap){
        lender = rulesMap.get("lender");
        for (Entry<String, String> entry : lender.entrySet()){
            String value = entry.getValue();
            entry.setValue(value.substring(1, value.length()-1));
        }
        borrower = rulesMap.get("borrower");
        for (Entry<String, String> entry : borrower.entrySet()){
            String value = entry.getValue();
            entry.setValue(value.substring(1, value.length()-1));
        }
    }

    public Map<String,String> getBorrower() {
        return borrower;
    }

    public Map<String,String> getLender() {
        return lender;
    }
    
}