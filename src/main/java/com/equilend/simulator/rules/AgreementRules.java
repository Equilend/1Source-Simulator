package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.trade.Trade;

public class AgreementRules implements Rules{

    private List<AgreementRule> rules = new ArrayList<>();
    private String partyId;

    public AgreementRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("lender").get("rules"));
        this.partyId = rulesMap.get("general").get("bot_party_id");
        rules.forEach(System.out::println);
    }

    public void addRules(String rulesList){
        if (rulesList.charAt(0) != '{') return;

        int start = rulesList.indexOf("(");
        while (start != -1){
            int end = rulesList.indexOf(")", start);
            
            String rule = rulesList.substring(start, end+1);
            rules.add(new AgreementRule(rule, partyId));
            
            start = rulesList.indexOf("(", end);
        }
    }
    
    public boolean shouldIgnoreTrade(Trade trade){
        for (AgreementRule rule : rules){
            if (rule.isApplicable(trade)){
                return rule.isShouldIgnore();
            }
        }

        return false;
    }
    
}