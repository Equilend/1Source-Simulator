package com.equilend.simulator.configurator.rules.recall_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.configurator.rules.Rules;
import com.os.client.model.Loan;
import com.os.client.model.Recall;

public class RecallRules implements Rules {

    private static final Logger logger = LogManager.getLogger(RecallRules.class.getName());
    private final List<RecallRule> cancelRules = new ArrayList<>();
    private final List<RecallRule> proposeRules = new ArrayList<>();
    private final List<RecallRule> acknowledgeRule = new ArrayList<>();
    private boolean analysisMode;

    private enum RecallRuleType {
        ACKNOWLEDGE,
        PROPOSE,
        CANCEL
    }

    public RecallRules() {
    }

    public RecallRules(Map<String, Map<String, String>> rulesMap) {
        if (rulesMap.containsKey("general")) {
            analysisMode = "1".equals(rulesMap.get("general").get("analysis_mode"));
        }
        if (rulesMap.containsKey("common")) {
            addRules(rulesMap.get("common").get("recall"), proposeRules, RecallRuleType.PROPOSE);
            addRules(rulesMap.get("common").get("cancel"), cancelRules, RecallRuleType.CANCEL);
            addRules(rulesMap.get("common").get("acknowledge"), acknowledgeRule, RecallRuleType.ACKNOWLEDGE);
        }
    }

    public void addRules(String rawRulesList, List<RecallRule> returnRules, RecallRuleType type) {
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
            RecallRule rule;
            switch (type) {
                case PROPOSE:
                    rule = new RecallProposeRule(ruleStr);
                    break;
                case CANCEL:
                    rule = new RecallCancelRule(ruleStr);
                    break;
                case ACKNOWLEDGE:
                    rule = new RecallAcknowledgeRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            returnRules.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public RecallCancelRule getRecallCancelRule(Recall recall, Loan loan,
        String botPartyId) {
        for (RecallRule rule : cancelRules) {
            RecallCancelRule recallCancelRule = (RecallCancelRule) rule;
            if (recallCancelRule.isApplicable(recall, loan, botPartyId)) {
                return recallCancelRule;
            }
        }
        return null;
    }

    public RecallProposeRule getRecallProposeRule(Loan loan,
        String botPartyId) {
        for (RecallRule rule : proposeRules) {
            RecallProposeRule recallProposeRule = (RecallProposeRule) rule;
            if (recallProposeRule.isApplicable(loan, botPartyId)) {
                return recallProposeRule;
            }
        }
        return null;
    }

    public RecallAcknowledgeRule getRecallAcknowledgeRule(Recall recall, Loan loan,
        String botPartyId) {
        for (RecallRule rule : acknowledgeRule) {
            RecallAcknowledgeRule recallAcknowledgeRule = (RecallAcknowledgeRule) rule;
            if (recallAcknowledgeRule.isApplicable(recall, loan, botPartyId)) {
                return recallAcknowledgeRule;
            }
        }
        return null;
    }
}
