package com.equilend.simulator.rules;

import java.util.Map;

public class AuthorizationRules implements Rules{

    private Map<String,String> borrower;
    private Map<String,String> lender;

    public AuthorizationRules(Map<String, Map<String, String>> rulesMap){
        lender = rulesMap.get("lender");
        borrower = rulesMap.get("borrower");
    }

    public Map<String,String> getBorrower() {
        return borrower;
    }

    public Map<String,String> getLender() {
        return lender;
    }
    
}