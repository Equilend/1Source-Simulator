package com.equilend.simulator.configurator.rules.agreement_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.configurator.rules.Rules;
import com.os.client.model.VenueTradeAgreement;

public class AgreementRules implements Rules {

    private final List<AgreementProposeRule> rules = new ArrayList<>();

    public AgreementRules() {
    }

    public AgreementRules(Map<String, Map<String, String>> rulesMap) {
        if (rulesMap.containsKey("initiator")) {
            addRules(rulesMap.get("initiator").get("incoming"));
        }
    }

    private void addRules(String rulesList) {
        if (rulesList == null) {
            return;
        }
        if (rulesList.charAt(0) != '{') {
            return;
        }

        int start = rulesList.indexOf(";(");
        while (start != -1) {
            int end = rulesList.indexOf(");", start);

            String rule = rulesList.substring(start + 1, end + 1);
            AgreementProposeRule agreementProposeRule = new AgreementProposeRule(rule);
            rules.add(agreementProposeRule);

            start = rulesList.indexOf(";(", end);
        }
    }

    public AgreementProposeRule getAgreementProposeRule(VenueTradeAgreement venueTradeAgreement, String partyId) {
        for (AgreementProposeRule rule : rules) {
            if (rule.isApplicable(venueTradeAgreement, partyId)) {
                return rule;
            }
        }
        return null;
    }

}