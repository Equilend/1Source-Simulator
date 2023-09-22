package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.contract.Contract;

public class ContractRules implements Rules{

    private List<ContractRule> borrowerRules = new ArrayList<>();
    private List<ContractRule> lenderRules = new ArrayList<>();
    private List<ContractRule> schedulerRules = new ArrayList<>();

    public ContractRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("borrower").get("responsive"), borrowerRules, true);
        addRules(rulesMap.get("lender").get("responsive"), lenderRules, true);
        addRules(rulesMap.get("lender").get("generative"), schedulerRules, false);
    }

    public void addRules(String rawRulesList, List<ContractRule> contractRulesList, boolean isResponsive){
        if (rawRulesList.charAt(0) != '{') return;

        int start = rawRulesList.indexOf("(")-1;
        while (start != -1){
            int end = rawRulesList.indexOf("),", start);
            
            String ruleStr = rawRulesList.substring(start+1, end+1);
            ContractRule rule = (isResponsive) ? new ContractResponsiveRule(ruleStr) : new ContractGenerativeRule(ruleStr);
            contractRulesList.add(rule);
            
            start = rawRulesList.indexOf(",(", end);
        }
    }

    public List<ContractRule> getSchedulerRules(){
        return schedulerRules;
    }

    //if should ignore trade return -1, otherwise return delay
    public Double shouldIgnoreTrade(Contract contract, String partyId){
        for (ContractRule rule : lenderRules){
            ContractResponsiveRule responsiveRule = (ContractResponsiveRule) rule;
            if (responsiveRule.isApplicable(contract, partyId)){
                return responsiveRule.isShouldIgnore() ? -1.0 : responsiveRule.getDelay();
            }
        }
        return -1.0;
    }

    public ContractResponsiveRule getApproveOrRejectApplicableRule(Contract contract, String partyId){
        for (ContractRule rule : borrowerRules){
            ContractResponsiveRule responsiveRule = (ContractResponsiveRule) rule;
            if (responsiveRule.isApplicable(contract, partyId)){
                return responsiveRule;
            }
        }
        return null;
    }
    
}