package com.equilend.simulator.configurator.rules.contract_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.configurator.rules.Rules;

public class ContractRules implements Rules{

    private List<ContractRule> recipientIncomingRules = new ArrayList<>();
    private List<ContractRule> initiatorIncomingRules = new ArrayList<>();
    private List<ContractRule> initiatorOutgoingRules = new ArrayList<>();

    public ContractRules(Map<String, Map<String, String>> rulesMap){        
        addRules(rulesMap.get("recipient").get("incoming"), recipientIncomingRules, true);
        addRules(rulesMap.get("initiator").get("incoming"), initiatorIncomingRules, true);
        addRules(rulesMap.get("initiator").get("outgoing"), initiatorOutgoingRules, false);
    }

    public void addRules(String rawRulesList, List<ContractRule> contractRulesList, boolean isResponsive){
        if (rawRulesList == null) return;
        if (rawRulesList.charAt(0) != '{') return;

        int start = rawRulesList.indexOf(",(");
        while (start != -1){
            int end = rawRulesList.indexOf("),", start);
            
            String ruleStr = rawRulesList.substring(start+1, end+1);
            ContractRule rule = (isResponsive) ? new ContractResponsiveRule(ruleStr) : new ContractGenerativeRule(ruleStr);
            contractRulesList.add(rule);

            start = rawRulesList.indexOf(",(", end);
        }
    }

    public List<ContractRule> getSchedulerRules(){
        return initiatorOutgoingRules;
    }

    public boolean schedulerMode(){
        return initiatorOutgoingRules.size() > 0;
    }

    //if should ignore trade return -1, otherwise return delay to cancel
    public Double shouldIgnoreTrade(Contract contract, String partyId){
        for (ContractRule rule : initiatorIncomingRules){
            ContractResponsiveRule responsiveRule = (ContractResponsiveRule) rule;
            if (responsiveRule.isApplicable(contract, partyId)){
                return responsiveRule.isShouldIgnore() ? -1.0 : responsiveRule.getDelay();
            }
        }
        return -1.0;
    }

    public ContractResponsiveRule getApproveOrRejectApplicableRule(Contract contract, String partyId){
        for (ContractRule rule : recipientIncomingRules){
            ContractResponsiveRule responsiveRule = (ContractResponsiveRule) rule;
            if (responsiveRule.isApplicable(contract, partyId)){
                return responsiveRule;
            }
        }
        return null;
    }
    
}