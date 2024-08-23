package com.equilend.simulator.configurator.rules.recall_rules;

import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.recall.Recall;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecallRules implements Rules {

    private static final Logger logger = LogManager.getLogger(RecallRules.class.getName());
    private final List<RecallRule> cancelRules = new ArrayList<>();
    private final List<RecallRule> proposeRules = new ArrayList<>();
    private final boolean analysisMode;

    private enum RecallRuleType {
        PROPOSE,
        CANCEL
    }

    public RecallRules(Map<String, Map<String, String>> rulesMap) {
        analysisMode = rulesMap.get("general").get("analysis_mode").equals("1");
        addRules(rulesMap.get("initiator").get("recall"), proposeRules, RecallRuleType.PROPOSE);
        addRules(rulesMap.get("initiator").get("cancel"), cancelRules, RecallRuleType.CANCEL);
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
                default:
                    rule = null;
            }
            returnRules.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public RecallCancelRule getRecallCancelRule(Recall recall, Contract contract,
        String botPartyId) {
        for (RecallRule rule : cancelRules) {
            RecallCancelRule recallCancelRule = (RecallCancelRule) rule;
            if (recallCancelRule.isApplicable(recall, contract, botPartyId)) {
                return recallCancelRule;
            }
        }
        return null;
    }

    public RecallProposeRule getRecallProposeRule(Contract contract,
        String botPartyId) {
        for (RecallRule rule : proposeRules) {
            RecallProposeRule recallProposeRule = (RecallProposeRule) rule;
            if (recallProposeRule.isApplicable(contract, botPartyId)) {
                return recallProposeRule;
            }
        }
        return null;
    }
}
