package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.trade.Trade;

public class AgreementRules implements Rules{

    private List<AgreementRule> rules = new ArrayList<>();

    public AgreementRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("general").get("responsive"));
    }

    public void addRules(String rulesList){
        if (rulesList.charAt(0) != '{') return;

        int start = rulesList.indexOf("(")-1;
        while (start != -1){
            int end = rulesList.indexOf("),", start);
            
            String rule = rulesList.substring(start+1, end+1);
            rules.add(new AgreementRule(rule));
            
            start = rulesList.indexOf(",(", end);
        }
    }
    
    public boolean shouldIgnoreTrade(Trade trade, String partyId){
        for (AgreementRule rule : rules){
            if (rule.isApplicable(trade, partyId)){
                return rule.isShouldIgnore();
            }
        }

        return false;
    }
    
}