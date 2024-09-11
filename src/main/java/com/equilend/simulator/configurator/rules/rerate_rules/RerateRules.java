package com.equilend.simulator.configurator.rules.rerate_rules;

import com.equilend.simulator.api.FedAPIException;
import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.rerate.Rerate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RerateRules implements Rules {

    private static final Logger logger = LogManager.getLogger(RerateRules.class.getName());
    private final List<RerateRule> approveRules = new ArrayList<>();
    private final List<RerateRule> cancelRules = new ArrayList<>();
    private final List<RerateRule> proposeRules = new ArrayList<>();
    private final List<RerateRule> pendingCancelRules = new ArrayList<>();
    private boolean analysisMode;

    public RerateRules() {
    }

    public RerateRules(Map<String, Map<String, String>> rulesMap) {
        if (rulesMap.containsKey("general")) {
            analysisMode = "1".equals(rulesMap.get("general").get("analysis_mode"));
        }
        if(rulesMap.containsKey("common")) {
            addRules(rulesMap.get("common").get("cancel"), cancelRules, RerateRuleType.CANCEL);
            addRules(rulesMap.get("common").get("approve"), approveRules, RerateRuleType.APPROVE);
            addRules(rulesMap.get("common").get("propose"), proposeRules, RerateRuleType.PROPOSE);
            addRules(rulesMap.get("common").get("cancel_pending"), pendingCancelRules, RerateRuleType.PENDING_CANCEL);
        }
    }

    private enum RerateRuleType {
        APPROVE,
        CANCEL,
        PROPOSE,
        PENDING_CANCEL
    }

    public void addRules(String rawRulesList, List<RerateRule> rerateRulesList, RerateRuleType type) {
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
            RerateRule rule;
            switch (type) {
                case APPROVE:
                    rule = new RerateApproveRule(ruleStr);
                    break;
                case CANCEL:
                    rule = new RerateCancelRule(ruleStr);
                    break;
                case PROPOSE:
                    rule = new RerateProposeRule(ruleStr);
                    break;
                case PENDING_CANCEL:
                    rule = new ReratePendingCancelRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            rerateRulesList.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public boolean getAnalysisMode() {
        return analysisMode;
    }

    //returns first applicable approve/reject rule
    public RerateApproveRule getApproveRule(Rerate rerate, Loan loan, String partyId) {
        for (RerateRule rule : approveRules) {
            RerateApproveRule approveRule = (RerateApproveRule) rule;
            try {
                if (approveRule.isApplicable(rerate, loan, partyId)) {
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
    public RerateCancelRule getCancelRule(Rerate rerate, Loan loan, String partyId) {
        for (RerateRule rule : cancelRules) {
            RerateCancelRule cancelRule = (RerateCancelRule) rule;
            try {
                if (cancelRule.isApplicable(rerate, loan, partyId)) {
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
    public RerateProposeRule getProposeRule(Loan loan, String partyId) {
        for (RerateRule rule : proposeRules) {
            RerateProposeRule proposeRule = (RerateProposeRule) rule;
            try {
                if (proposeRule.isApplicable(loan, partyId)) {
                    return proposeRule;
                }
            } catch (FedAPIException e) {
                logger.error("FEDAPIException error.. unable to get benchmark rate properly");
                return null;
            }
        }
        return null;
    }

    //returns first applicable propose/ignore rule
    public ReratePendingCancelRule getPendingCancelRule(Rerate rerate, Loan loan, String partyId) {
        for (RerateRule rule : pendingCancelRules) {
            ReratePendingCancelRule reratePendingCancelRule = (ReratePendingCancelRule) rule;
            try {
                if (reratePendingCancelRule.isApplicable(rerate, loan, partyId)) {
                    return reratePendingCancelRule;
                }
            } catch (FedAPIException e) {
                logger.error("FEDAPIException error.. unable to get benchmark rate properly");
                return null;
            }
        }
        return null;
    }

}