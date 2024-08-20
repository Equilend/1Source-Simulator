package com.equilend.simulator.configurator.rules.buyin_rules;

import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.configurator.rules.return_rules.ReturnCancelRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnRule;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.returns.Return;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyinRules implements Rules {

    private static final Logger logger = LogManager.getLogger(BuyinRules.class.getName());

    private final List<BuyinRule> acceptRules = new ArrayList<>();
    private final boolean analysisMode;

    private enum BuyinRuleType {
        ACCEPT
    }

    public BuyinRules(Map<String, Map<String, String>> rulesMap) {
        analysisMode = rulesMap.get("general").get("analysis_mode").equals("1");
        addRules(rulesMap.get("recipient").get("accept"), acceptRules, BuyinRuleType.ACCEPT);
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
                  default:
                    rule = null;
            }
            buyinRules.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public BuyinAcceptRule getBuyinAcceptRule(BuyinComplete buyinComplete, Contract contract,
        String botPartyId) {
        for (BuyinRule rule : acceptRules) {
            BuyinAcceptRule buyinAcceptRule = (BuyinAcceptRule) rule;
            if (buyinAcceptRule.isApplicable(buyinComplete, contract, botPartyId)) {
                return buyinAcceptRule;
            }
        }
        return null;
    }
}
