package com.equilend.simulator.configurator.rules.split_rules;

import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.split.LoanSplit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplitRules implements Rules {

    private static final Logger logger = LogManager.getLogger(SplitRules.class.getName());
    private final List<SplitRule> approveRules = new ArrayList<>();
    private final List<SplitRule> proposeRules = new ArrayList<>();
    private boolean analysisMode;

    private enum SplitRuleType {
        APPROVE,
        PROPOSE
    }

    public SplitRules() {
    }

    public SplitRules(Map<String, Map<String, String>> rulesMap) {
        if (rulesMap.containsKey("general")) {
            analysisMode = "1".equals(rulesMap.get("general").get("analysis_mode"));
        }
        if (rulesMap.containsKey("common")) {
            addRules(rulesMap.get("common").get("approve"), approveRules, SplitRuleType.APPROVE);
            addRules(rulesMap.get("common").get("split"), proposeRules, SplitRuleType.PROPOSE);
        }
    }

    public void addRules(String rawRulesList, List<SplitRule> splitRules, SplitRuleType type) {
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
            SplitRule rule;
            switch (type) {
                case APPROVE:
                    rule = new SplitApproveRule(ruleStr);
                    break;
                case PROPOSE:
                    rule = new SplitProposeRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            splitRules.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public SplitApproveRule getSplitApproveRule(LoanSplit loanSplit, Loan loan,
        String botPartyId) {
        for (SplitRule rule : approveRules) {
            SplitApproveRule splitApproveRule = (SplitApproveRule) rule;
            if (splitApproveRule.isApplicable(loanSplit, loan, botPartyId)) {
                return splitApproveRule;
            }
        }
        return null;
    }

    public SplitProposeRule getSplitProposeRule(Loan loan,
        String botPartyId) {
        for (SplitRule rule : proposeRules) {
            SplitProposeRule splitProposeRule = (SplitProposeRule) rule;
            if (splitProposeRule.isApplicable(loan, botPartyId)) {
                return splitProposeRule;
            }
        }
        return null;
    }
}
