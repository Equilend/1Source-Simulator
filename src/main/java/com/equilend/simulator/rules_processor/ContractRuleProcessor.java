package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.service.ContractService.acceptContract;
import static com.equilend.simulator.service.ContractService.cancelContract;
import static com.equilend.simulator.service.ContractService.declineContract;
import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.contract_rules.ContractApproveRejectRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractCancelRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractPendingCancelRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractPendingUpdateRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.settlement.SettlementStatus;
import com.equilend.simulator.service.ContractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContractRuleProcessor {

    private static final Logger logger = LogManager.getLogger(ContractRuleProcessor.class.getName());

    public static void process(Long startTime, ContractRule rule, Contract contract, String botPartyId)  throws APIException {

        if (rule instanceof ContractApproveRejectRule) {
            processByContractApproveRejectRule(startTime, (ContractApproveRejectRule) rule, contract, botPartyId);
        }
    }

    public static void process(Long startTime, ContractRule rule, Contract contract)
        throws APIException {

        if (rule instanceof ContractCancelRule) {
            processByContractCancelRule(startTime, (ContractCancelRule) rule, contract);
        }

        if (rule instanceof ContractPendingCancelRule) {
            processByPendingCancelRule(startTime, (ContractPendingCancelRule) rule, contract);
        }

        if (rule instanceof ContractPendingUpdateRule) {
            processByPendingUpdateRule(startTime, (ContractPendingUpdateRule) rule, contract);
        }
    }

    private static void processByContractApproveRejectRule(Long startTime, ContractApproveRejectRule rule,
        Contract contract, String botPartyId) throws APIException {
        if (rule.shouldApprove()) {
            PartyRole partyRole = ContractService.getTransactingPartyById(contract, botPartyId)
                .get()
                .getPartyRole();
            waitForDelay(startTime, rule.getDelay());
            acceptContract(contract.getContractId(), partyRole);
        }

        if (rule.shouldReject()) {
            waitForDelay(startTime, rule.getDelay());
            declineContract(contract.getContractId());
        }
    }

    private static void processByContractCancelRule(Long startTime, ContractCancelRule rule, Contract contract)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        cancelContract(contract.getContractId());
    }


    private static void processByPendingCancelRule(Long startTime, ContractPendingCancelRule rule, Contract contract)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        ContractService.cancelPendingContract(contract.getContractId());
    }

    private static void processByPendingUpdateRule(Long startTime, ContractPendingUpdateRule rule, Contract contract)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        ContractService.updateContractSettlementStatus(contract.getContractId(), SettlementStatus.SETTLED);
    }
}
