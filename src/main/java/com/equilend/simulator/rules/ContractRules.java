package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.contract.Contract;

public class ContractRules implements Rules{

        private List<ContractRule> borrowerRules = new ArrayList<>();
        // private List<ContractRule> lenderRules = new ArrayList<>();
        private String partyId;

    public ContractRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("borrower").get("rules"), borrowerRules);
        // addRules(rulesMap.get("lender").get("rules"), lenderRules);
        this.partyId = rulesMap.get("general").get("bot_party_id");
    }

    public void addRules(String rawRulesList, List<ContractRule> contractRulesList){
        if (rawRulesList.charAt(0) != '{') return;

        int start = rawRulesList.indexOf("(")-1;
        while (start != -1){
            int end = rawRulesList.indexOf("),", start);
            
            String rule = rawRulesList.substring(start+1, end+1);
            contractRulesList.add(new ContractRule(rule, partyId));
            
            start = rawRulesList.indexOf(",(", end);
        }
    }
    
    public boolean shouldRejectTrade(Contract contract){
        for (ContractRule rule : borrowerRules){
            if (rule.isApplicable(contract)){
                return rule.isShouldReject();
            }
        }

        return false;
    }
    
}