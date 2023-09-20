package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.contract.Contract;

public class ContractRules implements Rules{

    private List<ContractRule> borrowerRules = new ArrayList<>();
    private List<ContractRule> lenderRules = new ArrayList<>();

    private static final Logger logger = LogManager.getLogger();

    public ContractRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("borrower").get("responsive"), borrowerRules);
        addRules(rulesMap.get("lender").get("responsive"), lenderRules);
    }

    public void addRules(String rawRulesList, List<ContractRule> contractRulesList){
        if (rawRulesList.charAt(0) != '{') return;

        int start = rawRulesList.indexOf("(")-1;
        while (start != -1){
            int end = rawRulesList.indexOf("),", start);
            
            String rule = rawRulesList.substring(start+1, end+1);
            contractRulesList.add(new ContractRule(rule));
            
            start = rawRulesList.indexOf(",(", end);
        }
    }

    public boolean shouldIgnoreTrade(Contract contract, String partyId){
        for (ContractRule rule : lenderRules){
            if (rule.isApplicable(contract, partyId)){
                logger.info("Applying rule {}", rule);
                return rule.isShouldIgnore();
            }
        }
        logger.info("No rules applicable! Default to Ignore");
        return true;
    }

    public boolean shouldApproveTrade(Contract contract, String partyId){
        for (ContractRule rule : borrowerRules){
            if (rule.isApplicable(contract, partyId)){
                return rule.isShouldApprove();
            }
        }
        return false;
    }
    
}