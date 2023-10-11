package com.equilend.simulator.configurator.rules.rerate_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.FedAPIException;
import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.rerate.Rerate;

public class RerateRules implements Rules {

    private List<RerateRule> approveRules = new ArrayList<>();
    private List<RerateRule> cancelRules = new ArrayList<>();
    private List<RerateRule> proposeRules = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger();

    public RerateRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("recipient").get("approve"), approveRules, RerateRuleType.APPROVE);
        addRules(rulesMap.get("initiator").get("cancel"), cancelRules, RerateRuleType.CANCEL);
        addRules(rulesMap.get("initiator").get("propose"), proposeRules, RerateRuleType.PROPOSE);
    }

    private enum RerateRuleType{
        APPROVE,
        CANCEL,
        PROPOSE
    }

    public void addRules(String rawRulesList, List<RerateRule> rerateRulesList, RerateRuleType type){
        if (rawRulesList == null) return;
        if (rawRulesList.charAt(0) != '{') return;

        int start = rawRulesList.indexOf(",(");
        while (start != -1){
            int end = rawRulesList.indexOf("),", start);
            
            String ruleStr = rawRulesList.substring(start+1, end+1);
            RerateRule rule;
            switch (type){
                case APPROVE:
                    rule = new RerateApproveRule(ruleStr);
                    break;
                case CANCEL:
                    rule = new RerateCancelRule(ruleStr);
                    break;
                case PROPOSE:
                    rule = new RerateProposeRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            rerateRulesList.add(rule);

            start = rawRulesList.indexOf(",(", end);
        }
    }

    //returns first applicable approve/reject rule
    public RerateApproveRule getApproveRule(Rerate rerate, Contract contract, String partyId){
        for (RerateRule rule : approveRules){
            RerateApproveRule approveRule = (RerateApproveRule) rule;
            try {
                if (approveRule.isApplicable(rerate, contract, partyId)){
                    return approveRule;
                }
            } catch (FedAPIException e) {
                logger.error("FEDAPIException error.. unable to get benchmark rate properly");
                return null;
            }
        }
        return null;
    }

    //returns first applicable cancel/ignore rule
    public RerateCancelRule getCancelRule(Rerate rerate, Contract contract, String partyId){
        for (RerateRule rule : cancelRules){
            RerateCancelRule cancelRule = (RerateCancelRule) rule;
            try {
                if (cancelRule.isApplicable(rerate, contract, partyId)){
                    return cancelRule;
                }
            } catch (FedAPIException e) {
                logger.error("FEDAPIException error.. unable to get benchmark rate properly");
                return null;
            }
        }
        return null;
    } 

    //returns first applicable propose/ignore rule    
    public RerateProposeRule getProposeRule(Contract contract, String partyId){
        for (RerateRule rule : proposeRules){
            RerateProposeRule proposeRule = (RerateProposeRule) rule;
            try {
                if (proposeRule.isApplicable(contract, partyId)){
                    return proposeRule;
                }
            } catch (FedAPIException e) {
                logger.error("FEDAPIException error.. unable to get benchmark rate properly");
                return null;
            }
        }        

        return null;
    }

}