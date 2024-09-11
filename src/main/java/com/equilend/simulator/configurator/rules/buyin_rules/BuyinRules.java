package com.equilend.simulator.configurator.rules.buyin_rules;

import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.recall.Recall;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyinRules implements Rules {

    private static final Logger logger = LogManager.getLogger(BuyinRules.class.getName());

    private final List<BuyinRule> acceptRules = new ArrayList<>();
    private final List<BuyinRule> proposeRules = new ArrayList<>();
    private boolean analysisMode;

    private enum BuyinRuleType {
        ACCEPT,
        PROPOSE
    }

    public BuyinRules() {
    }

    public BuyinRules(Map<String, Map<String, String>> rulesMap) {
        if (rulesMap.containsKey("general")) {
            analysisMode = "1".equals(rulesMap.get("general").get("analysis_mode"));
        }
        if (rulesMap.containsKey("common")) {
            addRules(rulesMap.get("common").get("accept"), acceptRules, BuyinRuleType.ACCEPT);
            addRules(rulesMap.get("common").get("submit"), proposeRules, BuyinRuleType.PROPOSE);
        }
    }

    public void addRules(String rawRulesList, List<BuyinRule> buyinRules, BuyinRuleType type) {
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
            BuyinRule rule;
            switch (type) {
                case ACCEPT:
                    rule = new BuyinAcceptRule(ruleStr);
                    break;
                case PROPOSE:
                    rule = new BuyinProposeRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            buyinRules.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public BuyinAcceptRule getBuyinAcceptRule(BuyinComplete buyinComplete, Loan loan,
        String botPartyId) {
        for (BuyinRule rule : acceptRules) {
            BuyinAcceptRule buyinAcceptRule = (BuyinAcceptRule) rule;
            if (buyinAcceptRule.isApplicable(buyinComplete, loan, botPartyId)) {
                return buyinAcceptRule;
            }
        }
        return null;
    }

    public BuyinProposeRule getBuyinProposeRule(Recall recall, Loan loan,
        String botPartyId) {
        for (BuyinRule rule : proposeRules) {
            BuyinProposeRule buyinProposeRule = (BuyinProposeRule) rule;
            if (buyinProposeRule.isApplicable(recall, loan, botPartyId)) {
                return buyinProposeRule;
            }
        }
        return null;
    }
}
