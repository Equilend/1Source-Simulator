package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.model.collateral.RoundingMode.ALWAYSUP;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractResponsiveRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.contract.ContractProposalApproval;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.settlement.PartySettlementInstruction;
import com.equilend.simulator.service.ContractService;
import com.equilend.simulator.service.SettlementService;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContractHandler implements EventHandler {

    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;
    private static final Logger logger = LogManager.getLogger();

    public ContractHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.botPartyId = configurator.getGeneralRules().getBotPartyId();
        this.startTime = startTime;
    }

    public Contract getContractById(String id) {
        Contract contract = null;
        try {
            contract = APIConnector.getContractById(EventHandler.getToken(), id);
        } catch (APIException e) {
            logger.debug("Unable to process contract event");
        }
        if (contract == null) {
            logger.trace("get contract by id returns null");
        }
        return contract;
    }

    public static boolean didBotInitiate(String botPartyId, Contract contract) {
        // Currently, lender only provides its settlement info it only has lender settlement on contract
        //But borrower creates both lender and borrower settlement, even if lender is empty
        boolean lenderInitiated = contract.getSettlement().size() == 1;
        Optional<TransactingParty> transactingPartyOptional = ContractService.getTransactingPartyById(contract, botPartyId);
        if (lenderInitiated) {
            return transactingPartyOptional.isPresent()
                && transactingPartyOptional.get().getPartyRole() == PartyRole.LENDER;
        }
        //Of course, this won't work if lender provides both its own and the borrower's settlement info
        //But this is the best we can do until initiator party id given in contract json
        return transactingPartyOptional.isPresent()
            && transactingPartyOptional.get().getPartyRole() == PartyRole.BORROWER;
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
        PartySettlementInstruction partySettlementInstruction = SettlementService.createPartySettlementInstruction(role);
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

        //Get contract by Id
        Contract contract = getContractById(contractId);
        if (contract == null) {
            return;
        }

        boolean botInitiated = didBotInitiate(botPartyId, contract);
        if (botInitiated) {
            Double delay = configurator.getContractRules().shouldIgnoreTrade(contract, botPartyId);
            if (delay == -1) {
                return;
            }
            cancelContractProposal(contractId, startTime, delay);
        } else {
            //Analyze contract to decide whether to accept or decline based on configurator
            ContractResponsiveRule rule = configurator.getContractRules()
                .getApproveOrRejectApplicableRule(contract, botPartyId);
            if (rule == null) {
                //If no applicable rule, then default to ignoring the contract.
                return;
            } else if (rule.isShouldApprove()) {
                PartyRole partyRole = ContractService.getTransactingPartyById(contract, botPartyId).get().getPartyRole();
                acceptContractProposal(contractId, partyRole, startTime,
                    rule.getDelay());
            } else {
                declineContractProposal(contractId, startTime, rule.getDelay());
            }
        }
    }

}