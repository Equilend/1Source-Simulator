package com.equilend.simulator.configurator.rules.loan_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.configurator.rules.Rules;
import com.os.client.model.Loan;

public class LoanRules implements Rules {

    private final List<LoanRule> loanApproveRejectRules = new ArrayList<>();
    private final List<LoanRule> loanCancelRules = new ArrayList<>();
    private final List<LoanRule> loanProposeRules = new ArrayList<>();
    private final List<LoanRule> loanPendingCancelRules = new ArrayList<>();
    private final List<LoanRule> loanPendingUpdateRules = new ArrayList<>();
    private boolean analysisMode;
    private String analysisStartDate = APIConnector.formatTime(APIConnector.getCurrentTime()).substring(0, 10);

    private enum LoanRuleType {
        APPROVE_REJECT,
        CANCEL,
        PROPOSE,
        PENDING_CANCEL,
        PENDING_UPDATE;
    }

    public LoanRules() {
    }

    public LoanRules(Map<String, Map<String, String>> rulesMap) {
        if(rulesMap.containsKey("recipient")) {
            addRules(rulesMap.get("recipient").get("incoming"), loanApproveRejectRules,
                LoanRuleType.APPROVE_REJECT);
        }
        if(rulesMap.containsKey("initiator")) {
            addRules(rulesMap.get("initiator").get("incoming"), loanCancelRules, LoanRuleType.CANCEL);
            addRules(rulesMap.get("initiator").get("outgoing"), loanProposeRules, LoanRuleType.PROPOSE);
        }
        if(rulesMap.containsKey("common")) {
            addRules(rulesMap.get("common").get("cancel_pending"), loanPendingCancelRules,
                LoanRuleType.PENDING_CANCEL);
            addRules(rulesMap.get("common").get("update_settlement"), loanPendingUpdateRules,
                LoanRuleType.PENDING_UPDATE);
        }
        if(rulesMap.containsKey("general")) {
            analysisMode ="1".equals(rulesMap.get("general").get("analysis_mode"));
            analysisStartDate = rulesMap.get("general").get("analysis_start_date");
        }
    }

    private void addRules(String rawRulesList, List<LoanRule> loanRulesList, LoanRuleType type) {
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
            LoanRule rule;
            switch (type) {
                case APPROVE_REJECT:
                    rule = new LoanApproveRejectRule(ruleStr);
                    break;
                case CANCEL:
                    rule = new LoanCancelRule(ruleStr);
                    break;
                case PROPOSE:
                    rule = new LoanGenerativeRule(ruleStr);
                    break;
                case PENDING_CANCEL:
                    rule = new LoanPendingCancelRule(ruleStr);
                    break;
                case PENDING_UPDATE:
                    rule = new LoanPendingUpdateRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            loanRulesList.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public List<LoanRule> getLoanProposeRules() {
        return loanProposeRules;
    }

    public boolean schedulerMode() {
        return !loanProposeRules.isEmpty();
    }

    public boolean getAnalysisMode() {
        return analysisMode;
    }

    public String getAnalysisStartDate() {
        return analysisStartDate;
    }

    public LoanApproveRejectRule getLoanApproveRejectRule(Loan loan,
        String botPartyId) {
        for (LoanRule rule : loanApproveRejectRules) {
            LoanApproveRejectRule loanApproveRejectRule = (LoanApproveRejectRule) rule;
            if (loanApproveRejectRule.isApplicable(loan, botPartyId)) {
                return loanApproveRejectRule;
            }
        }
        return null;
    }

    public LoanCancelRule getLoanCancelRule(Loan loan,
        String botPartyId) {
        for (LoanRule rule : loanCancelRules) {
            LoanCancelRule loanCancelRule = (LoanCancelRule) rule;
            if (loanCancelRule.isApplicable(loan, botPartyId)) {
                return loanCancelRule;
            }
        }
        return null;
    }

    public LoanPendingCancelRule getLoanPendingCancelRule(Loan loan,
        String botPartyId) {
        for (LoanRule rule : loanPendingCancelRules) {
            LoanPendingCancelRule loanPendingCancelRule = (LoanPendingCancelRule) rule;
            if (loanPendingCancelRule.isApplicable(loan, botPartyId)) {
                return loanPendingCancelRule;
            }
        }
        return null;
    }

    public LoanPendingUpdateRule getLoanPendingUpdateRule(Loan loan,
        String botPartyId) {
        for (LoanRule rule : loanPendingUpdateRules) {
            LoanPendingUpdateRule loanPendingUpdateRule = (LoanPendingUpdateRule) rule;
            if (loanPendingUpdateRule.isApplicable(loan, botPartyId)) {
                return loanPendingUpdateRule;
            }
        }
        return null;
    }
}