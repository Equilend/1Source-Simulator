package com.equilend.simulator.configurator.rules.contract_rules;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.model.contract.Contract;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContractRules implements Rules {

    private final List<ContractRule> contractApproveRejectRules = new ArrayList<>();
    private final List<ContractRule> contractCancelRules = new ArrayList<>();
    private final List<ContractRule> contractProposeRules = new ArrayList<>();
    private final List<ContractRule> contractPendingCancelRules = new ArrayList<>();
    private final List<ContractRule> contractPendingUpdateRules = new ArrayList<>();
    private final boolean analysisMode;
    private String analysisStartDate = APIConnector.formatTime(APIConnector.getCurrentTime()).substring(0, 10);

    private enum ContractRuleType {
        APPROVE_REJECT,
        CANCEL,
        PROPOSE,
        PENDING_CANCEL,
        PENDING_UPDATE,
    }

    public ContractRules(Map<String, Map<String, String>> rulesMap) {
        addRules(rulesMap.get("recipient").get("incoming"), contractApproveRejectRules,
            ContractRuleType.APPROVE_REJECT);
        addRules(rulesMap.get("initiator").get("incoming"), contractCancelRules, ContractRuleType.CANCEL);
        addRules(rulesMap.get("initiator").get("outgoing"), contractProposeRules, ContractRuleType.PROPOSE);
        addRules(rulesMap.get("common").get("cancel_pending"), contractPendingCancelRules,
            ContractRuleType.PENDING_CANCEL);
        addRules(rulesMap.get("common").get("update_settlement"), contractPendingUpdateRules,
            ContractRuleType.PENDING_UPDATE);
        analysisMode = rulesMap.get("general").get("analysis_mode").equals("1");
        analysisStartDate = rulesMap.get("general").get("analysis_start_date");
    }

    public void addRules(String rawRulesList, List<ContractRule> contractRulesList, ContractRuleType type) {
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
            ContractRule rule;
            switch (type) {
                case APPROVE_REJECT:
                    rule = new ContractApproveRejectRule(ruleStr);
                    break;
                case CANCEL:
                    rule = new ContractCancelRule(ruleStr);
                    break;
                case PROPOSE:
                    rule = new ContractGenerativeRule(ruleStr);
                    break;
                case PENDING_CANCEL:
                    rule = new ContractPendingCancelRule(ruleStr);
                    break;
                case PENDING_UPDATE:
                    rule = new ContractPendingUpdateRule(ruleStr);
                    break;
                default:
                    rule = null;
            }
            contractRulesList.add(rule);

            start = rawRulesList.indexOf(";(", end);
        }
    }

    public List<ContractRule> getContractProposeRules() {
        return contractProposeRules;
    }

    public boolean schedulerMode() {
        return !contractProposeRules.isEmpty();
    }

    public boolean getAnalysisMode() {
        return analysisMode;
    }

    public String getAnalysisStartDate() {
        return analysisStartDate;
    }

    public ContractApproveRejectRule getContractApproveRejectRule(Contract contract,
        String botPartyId) {
        for (ContractRule rule : contractApproveRejectRules) {
            ContractApproveRejectRule contractApproveRejectRule = (ContractApproveRejectRule) rule;
            if (contractApproveRejectRule.isApplicable(contract, botPartyId)) {
                return contractApproveRejectRule;
            }
        }
        return null;
    }

    public ContractCancelRule getContractCancelRule(Contract contract,
        String botPartyId) {
        for (ContractRule rule : contractCancelRules) {
            ContractCancelRule contractCancelRule = (ContractCancelRule) rule;
            if (contractCancelRule.isApplicable(contract, botPartyId)) {
                return contractCancelRule;
            }
        }
        return null;
    }

    public ContractPendingCancelRule getContractPendingCancelRule(Contract contract,
        String botPartyId) {
        for (ContractRule rule : contractPendingCancelRules) {
            ContractPendingCancelRule contractPendingCancelRule = (ContractPendingCancelRule) rule;
            if (contractPendingCancelRule.isApplicable(contract, botPartyId)) {
                return contractPendingCancelRule;
            }
        }
        return null;
    }

    public ContractPendingUpdateRule getContractPendingUpdateRule(Contract contract,
        String botPartyId) {
        for (ContractRule rule : contractPendingUpdateRules) {
            ContractPendingUpdateRule contractPendingUpdateRule = (ContractPendingUpdateRule) rule;
            if (contractPendingUpdateRule.isApplicable(contract, botPartyId)) {
                return contractPendingUpdateRule;
            }
        }
        return null;
    }
}