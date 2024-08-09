package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.model.event.EventType.CONTRACT_OPENED;
import static com.equilend.simulator.model.event.EventType.RERATE_PENDING;
import static com.equilend.simulator.model.event.EventType.RERATE_PROPOSED;
import static com.equilend.simulator.service.ContractService.getTransactingPartyById;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.ReratePendingCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.rate.FixedRateDef;
import com.equilend.simulator.model.rate.Rate;
import com.equilend.simulator.model.rate.RebateRate;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.rerate.RerateProposal;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RerateHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(RerateHandler.class.getName());
    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;

    public RerateHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.startTime = startTime;
    }

    private Rerate getRerateById(String id) {
        Rerate rerate = null;
        try {
            rerate = APIConnector.getRerateById(EventHandler.getToken(), id);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }
        return rerate;
    }

    private Contract getContractById(String id) {
        Contract contract = null;
        try {
            contract = APIConnector.getContractById(EventHandler.getToken(), id);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }

        return contract;
    }

    public static void postRerateProposal(String contractId, Rate rate, Double delta, Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        FixedRateDef fee = rate.getFee();
        RebateRate rebate = rate.getRebate();

        if (fee != null) {
            fee.setBaseRate(Math.max(fee.getBaseRate() + delta, 0.01));
        } else if (rebate != null) {
            if (rebate.getFixed() != null) {
                rebate.getFixed().setBaseRate(rebate.getFixed().getBaseRate() + delta);
            } else if (rebate.getFloating() != null) {
                rebate.getFloating().setSpread(rebate.getFloating().getSpread() + delta);
            }
        }

        try {
            APIConnector.postRerateProposal(EventHandler.getToken(), contractId, new RerateProposal().rate(rate));
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }

    }

    public static void cancelRerateProposal(String contractId, String rerateId, Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        try {
            APIConnector.cancelRerateProposal(EventHandler.getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }
    }

    public static void cancelReratePending(String contractId, String rerateId, Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        try {
            APIConnector.cancelReratePending(EventHandler.getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }
    }

    public static void approveRerateProposal(String contractId, String rerateId, Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        try {
            APIConnector.approveRerateProposal(EventHandler.getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }
    }

    public static void declineRerateProposal(String contractId, String rerateId, Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        try {
            APIConnector.declineRerateProposal(EventHandler.getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }
    }

    public void run() {
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");

        if (event.getEventType().equals(RERATE_PROPOSED)) {
            String rerateId = arr[arr.length - 1];
            Rerate rerate = getRerateById(rerateId);
            if (rerate == null) {
                return;
            }

            String contractId = rerate.getContractId();
            Contract contract = getContractById(contractId);

            //For now, default to lender party being initiator and borrower party being recipient
            Optional<TransactingParty> transactingParty = getTransactingPartyById(contract, botPartyId);
            if (transactingParty.isPresent() && transactingParty.get().getPartyRole() == PartyRole.LENDER) {
                RerateCancelRule rule = configurator.getRerateRules().getCancelRule(rerate, contract, botPartyId);
                if (rule == null || !rule.shouldCancel()) {
                    return; //default to ignore/ not cancelling
                }
                Double delay = rule.getDelay();
                cancelRerateProposal(contractId, rerateId, startTime, delay);
            } else {
                RerateApproveRule rule = configurator.getRerateRules().getApproveRule(rerate, contract, botPartyId);
                if (rule == null) {
                    //no applicable rule, default to approve w/o delay
                    approveRerateProposal(contractId, rerateId, startTime, 0.0);
                } else if (rule.shouldApprove()) {
                    Double delay = rule.getDelay();
                    approveRerateProposal(contractId, rerateId, startTime, delay);
                } else {
                    Double delay = rule.getDelay();
                    declineRerateProposal(contractId, rerateId, startTime, delay);
                }
            }
        } else if (event.getEventType().equals(CONTRACT_OPENED)) {
            String contractId = arr[arr.length - 1];
            Contract contract = getContractById(contractId);

            RerateProposeRule rule = configurator.getRerateRules().getProposeRule(contract, botPartyId);
            if (rule == null || !rule.shouldPropose()) {
                return;
            }

            Double delta = rule.getDelta();
            Double delay = rule.getDelay();

            postRerateProposal(contractId, contract.getTrade().getRate(), delta, startTime, delay);
        } else if (event.getEventType().equals(RERATE_PENDING)) {
            String rerateId = arr[arr.length - 1];
            Rerate rerate = getRerateById(rerateId);
            if (rerate == null) {
                return;
            }

            String contractId = rerate.getContractId();
            Contract contract = getContractById(contractId);

            ReratePendingCancelRule rule = configurator.getRerateRules()
                .getPendingCancelRule(rerate, contract, botPartyId);
            if (rule == null || !rule.shouldCancel()) {
                return; //default to ignore/ not cancelling
            }
            Double delay = rule.getDelay();
            cancelReratePending(contractId, rerateId, startTime, delay);
        }
    }
}