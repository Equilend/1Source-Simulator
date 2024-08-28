package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.ContractService.acceptContract;
import static com.equilend.simulator.service.ContractService.cancelContract;
import static com.equilend.simulator.service.ContractService.declineContract;
import static com.equilend.simulator.service.ContractService.getContractById;
import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractApproveRejectRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractCancelRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractPendingCancelRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractPendingUpdateRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallProposeRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeFromContractRule;
import com.equilend.simulator.configurator.rules.split_rules.SplitProposeRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.rules_processor.ContractRuleProcessor;
import com.equilend.simulator.rules_processor.RecallRuleProcessor;
import com.equilend.simulator.rules_processor.RerateRuleProcessor;
import com.equilend.simulator.rules_processor.ReturnRuleProcessor;
import com.equilend.simulator.rules_processor.SplitRuleProcessor;
import com.equilend.simulator.service.ContractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContractHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(ContractHandler.class.getName());
    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;

    public ContractHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.startTime = startTime;
    }


    public void run() {
        //Parse contract id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String contractId = arr[arr.length - 1];

        try {
            Contract contract = getContractById(contractId);
            if (contract == null) {
                return;
            }

            boolean isInitiator = ContractService.isInitiator(contract, botPartyId);

            switch (event.getEventType()) {
                case CONTRACT_PROPOSED:
                    if (isInitiator) {
                        ContractCancelRule contractCancelRule = configurator.getContractRules()
                            .getContractCancelRule(contract, botPartyId);
                        if (contractCancelRule != null && contractCancelRule.shouldCancel()) {
                            ContractRuleProcessor.process(startTime, contractCancelRule, contract);
                          }
                    } else {
                        ContractApproveRejectRule contractApproveRejectRule = configurator.getContractRules()
                            .getContractApproveRejectRule(contract, botPartyId);
                        if (contractApproveRejectRule != null && !contractApproveRejectRule.shouldIgnore()) {
                            ContractRuleProcessor.process(startTime, contractApproveRejectRule, contract, botPartyId);
                        }
                    }
                    break;
                case CONTRACT_OPENED:
                    if (isInitiator) {
                        RerateProposeRule rerateProposeRule = configurator.getRerateRules()
                            .getProposeRule(contract, botPartyId);
                        if (rerateProposeRule != null && rerateProposeRule.shouldPropose()) {
                            RerateRuleProcessor.process(startTime, rerateProposeRule, contract, null);
                            return;
                        }

                        ReturnProposeFromContractRule returnProposeRule = configurator.getReturnRules()
                            .getReturnProposeFromContractRule(contract, botPartyId);
                        if (returnProposeRule != null && returnProposeRule.shouldPropose()) {
                            ReturnRuleProcessor.process(startTime, returnProposeRule, contract, null);
                            return;
                        }

                        RecallProposeRule recallProposeRule = configurator.getRecallRules()
                            .getRecallProposeRule(contract, botPartyId);
                        if (recallProposeRule != null && recallProposeRule.shouldPropose()) {
                            RecallRuleProcessor.process(startTime, recallProposeRule, contract, null);
                            return;
                        }

                        SplitProposeRule splitProposeRule = configurator.getSplitRules()
                            .getSplitProposeRule(contract, botPartyId);
                        if (splitProposeRule != null && splitProposeRule.shouldPropose()) {
                            SplitRuleProcessor.process(startTime, splitProposeRule, contract, null);
                            return;
                        }
                    }
                    break;

                case CONTRACT_PENDING:
                    ContractPendingCancelRule contractPendingCancelRule = configurator.getContractRules()
                        .getContractPendingCancelRule(contract, botPartyId);
                    if (contractPendingCancelRule != null && contractPendingCancelRule.shouldCancel()) {
                        ContractRuleProcessor.process(startTime, contractPendingCancelRule, contract);
                        return;
                    }

                    ContractPendingUpdateRule contractPendingUpdateRule = configurator.getContractRules()
                        .getContractPendingUpdateRule(contract, botPartyId);
                    if (contractPendingUpdateRule != null && contractPendingUpdateRule.shouldUpdate()) {
                        ContractRuleProcessor.process(startTime, contractPendingUpdateRule, contract);
                        return;
                    }
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.debug("Unable to process contract event", e);
        }

    }

}