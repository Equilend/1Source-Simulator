package com.equilend.simulator.configurator.rules.agreement_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.model.trade.Trade;
import com.equilend.simulator.configurator.rules.Rules;

public class AgreementRules implements Rules{

    private List<AgreementRule> rules = new ArrayList<>();

    public AgreementRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("initiator").get("incoming"));
    }

    public void addRules(String rulesList){
        if (rulesList == null) return;
        if (rulesList.charAt(0) != '{') return;

        int start = rulesList.indexOf(";(");
        while (start != -1){
            int end = rulesList.indexOf(");", start);
            
            String rule = rulesList.substring(start+1, end+1);
            AgreementRule agreementRule = new AgreementRule(rule);
            rules.add(agreementRule);
            
            start = rulesList.indexOf(";(", end);
        }
    }
    
    public AgreementRule getFirstApplicableRule(Trade trade, String partyId){
        for (AgreementRule rule : rules){
            if (rule.isApplicable(trade, partyId)){
                return rule;
            }
        }
        return null;
    }
    
}