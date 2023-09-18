package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgreementRules implements Rules{

    private List<AgreementRule> rules = new ArrayList<>();

    public AgreementRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("lender").get("rules"));

        rules.forEach(System.out::println);
    }

    public void addRules(String rulesList){
        if (rulesList.charAt(0) != '{') return;

        int start = rulesList.indexOf("(");
        while (start != -1){
            int end = rulesList.indexOf(")", start);
            
            String rule = rulesList.substring(start, end+1);
            rules.add(new AgreementRule(rule));
            
            start = rulesList.indexOf("(", end);
        }
    }    
    
}
