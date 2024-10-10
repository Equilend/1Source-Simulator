package com.equilend.simulator.configurator.rules.return_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.configurator.rules.Rules;
import com.os.client.model.Loan;
import com.os.client.model.ModelReturn;

public class ReturnRules implements Rules {

    private static final Logger logger = LogManager.getLogger(ReturnRules.class.getName());
    private final List<ReturnRule> acknowledgeRules = new ArrayList<>();
    private final List<ReturnRule> cancelRules = new ArrayList<>();
    private final List<ReturnRule> proposeFromLoanRules = new ArrayList<>();
    private final List<ReturnRule> proposeFromReturnRules = new ArrayList<>();
    private final List<ReturnRule> settlementStatusUpdateRules = new ArrayList<>();
    private boolean analysisMode;

    private enum ReturnRuleType {
        ACKNOWLEDGE,
        CANCEL,
        PROPOSE_FROM_LOAN,
        PROPOSE_FROM_RECALL,
        UPDATE;
    }

    public ReturnRules() {
    }

    public ReturnRules(Map<String, Map<String, String>> rulesMap) {
        if (rulesMap.containsKey("general")) {
            analysisMode = "1".equals(rulesMap.get("general").get("analysis_mode"));
        }
        if (rulesMap.containsKey("common")) {
            addRules(rulesMap.get("common").get("acknowledge"), acknowledgeRules, ReturnRuleType.ACKNOWLEDGE);
            addRules(rulesMap.get("common").get("cancel"), cancelRules, ReturnRuleType.CANCEL);
            addRules(rulesMap.get("common").get("return_from_recall"), proposeFromReturnRules,
                ReturnRuleType.PROPOSE_FROM_RECALL);
            addRules(rulesMap.get("common").get("return"), proposeFromLoanRules, ReturnRuleType.PROPOSE_FROM_LOAN);
            addRules(rulesMap.get("common").get("update_settlement"), settlementStatusUpdateRules,
                ReturnRuleType.UPDATE);
        }
    }

    public void addRules(String rawRulesList, List<ReturnRule> returnRules, ReturnRuleType type) {
        if (rawRulesList == null) {
            return;
        }
        if (rawRulesList.charAt(0) != '{') {
            return;
        }

        int start = rawRulesList.indexOf(";(");
        while (start != -1) {
            int end = rawRulesList.indexOf(");", start);

            String ruleStr = rawRulesList.substring(start + 1, end + 1);
            ReturnRule rule;
            switch (type) {
                case ACKNOWLEDGE:
                    rule = new ReturnAcknowledgeRule(ruleStr);
                    break;
                case CANCEL:
                    rule = new ReturnCancelRule(ruleStr);
                    break;
                case PROPOSE_FROM_LOAN:
                    rule = new ReturnProposeFromLoanRule(ruleStr);
                    break;
                case PROPOSE_FROM_RECALL:
                    rule = new ReturnProposeFromRecallRule(ruleStr);
                    break;
                case UPDATE:
                    rule = new ReturnSettlementStatusUpdateRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            returnRules.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public ReturnAcknowledgeRule getReturnAcknowledgeRule(ModelReturn oneSourceReturn, Loan loan,
        String botPartyId) {
        for (ReturnRule rule : acknowledgeRules) {
            ReturnAcknowledgeRule acknowledgeRule = (ReturnAcknowledgeRule) rule;
            if (acknowledgeRule.isApplicable(oneSourceReturn, loan, botPartyId)) {
                return acknowledgeRule;
            }
        }
        return null;
    }

    public ReturnCancelRule getReturnCancelRule(ModelReturn oneSourceReturn, Loan loan,
        String botPartyId) {
        for (ReturnRule rule : cancelRules) {
            ReturnCancelRule returnCancelRule = (ReturnCancelRule) rule;
            if (returnCancelRule.isApplicable(oneSourceReturn, loan, botPartyId)) {
                return returnCancelRule;
            }
        }
        return null;
    }

    public ReturnProposeFromLoanRule getReturnProposeFromLoanRule(Loan loan,
        String botPartyId) {
        for (ReturnRule rule : proposeFromLoanRules) {
            ReturnProposeFromLoanRule returnProposeRule = (ReturnProposeFromLoanRule) rule;
            if (returnProposeRule.isApplicable(loan, botPartyId)) {
                return returnProposeRule;
            }
        }
        return null;
    }

    public ReturnProposeFromRecallRule getReturnProposeFromRecallRule(Loan loan,
        String botPartyId) {
        for (ReturnRule rule : proposeFromReturnRules) {
            ReturnProposeFromRecallRule returnProposeRule = (ReturnProposeFromRecallRule) rule;
            if (returnProposeRule.isApplicable(loan, botPartyId)) {
                return returnProposeRule;
            }
        }
        return null;
    }

    public ReturnSettlementStatusUpdateRule getReturnSettlementStatusUpdateRule(ModelReturn oneSourceReturn, Loan loan,
        String botPartyId) {
        for (ReturnRule rule : settlementStatusUpdateRules) {
            ReturnSettlementStatusUpdateRule returnSettlementStatusUpdateRule = (ReturnSettlementStatusUpdateRule) rule;
            if (returnSettlementStatusUpdateRule.isApplicable(oneSourceReturn, loan, botPartyId)) {
                return returnSettlementStatusUpdateRule;
            }
        }
        return null;
    }
}
