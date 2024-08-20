package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.model.collateral.RoundingMode.ALWAYSUP;
import static com.equilend.simulator.service.ContractService.getContractById;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractResponsiveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.contract.ContractProposalApproval;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.settlement.PartySettlementInstruction;
import com.equilend.simulator.rules_processor.RerateRuleProcessor;
import com.equilend.simulator.rules_processor.ReturnRuleProcessor;
import com.equilend.simulator.service.ContractService;
import com.equilend.simulator.service.SettlementService;
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

    public static void cancelContractProposal(String contractId, Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        try {
            APIConnector.cancelContractProposal(EventHandler.getToken(), contractId);
        } catch (APIException e) {
            logger.debug("Unable to process contract event");
        }
    }

    public static void acceptContractProposal(String contractId, PartyRole role, Long startTime, Double delay) {
        PartySettlementInstruction partySettlementInstruction = SettlementService.createPartySettlementInstruction(
            role);
        ContractProposalApproval contractProposalApproval = new ContractProposalApproval()
            .settlement(partySettlementInstruction);
        if (role == PartyRole.LENDER) {
            contractProposalApproval = contractProposalApproval.roundingRule(10).roundingMode(ALWAYSUP);
        }

        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        try {
            APIConnector.acceptContractProposal(EventHandler.getToken(), contractId, contractProposalApproval);
        } catch (APIException e) {
            logger.debug("Unable to process contract event");
        }
    }

    public static void declineContractProposal(String contractId, Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        try {
            APIConnector.declineContractProposal(EventHandler.getToken(), contractId);
        } catch (APIException e) {
            logger.debug("Unable to process contract event");
        }
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
                        Double delay = configurator.getContractRules().shouldIgnoreTrade(contract, botPartyId);
                        if (delay == -1) {
                            return;
                        }
                        cancelContractProposal(contractId, startTime, delay);
                    } else {
                        //Analyze contract to decide whether to accept or decline based on configurator
                        ContractResponsiveRule rule = configurator.getContractRules()
                            .getApproveOrRejectApplicableRule(contract, botPartyId);
                        if (rule != null) {
                            if (rule.isShouldApprove()) {
                                PartyRole partyRole = ContractService.getTransactingPartyById(contract, botPartyId)
                                    .get()
                                    .getPartyRole();
                                acceptContractProposal(contractId, partyRole, startTime,
                                    rule.getDelay());
                            } else {
                                declineContractProposal(contractId, startTime, rule.getDelay());
                            }
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

                        ReturnProposeRule returnProposeRule = configurator.getReturnRules()
                            .getReturnProposeRule(contract, botPartyId);
                        if (returnProposeRule != null && returnProposeRule.shouldPropose()) {
                            ReturnRuleProcessor.process(startTime, returnProposeRule, contract, null);
                            return;
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        }catch (APIException e){
            logger.debug("Unable to process contract event", e);
        }

    }

}