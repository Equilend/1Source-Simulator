package com.equilend.simulator.events_processor.event_handler;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementRule;
import com.equilend.simulator.model.agreement.Agreement;
import com.equilend.simulator.model.contract.ContractProposal;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.trade.TradeAgreement;
import com.equilend.simulator.model.venue.VenueTradeAgreement;
import com.equilend.simulator.service.ContractService;
import com.equilend.simulator.service.TradeService;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TradeHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(TradeHandler.class.getName());
    private final Event event;
    private Agreement agreement;
    private final Configurator configurator;
    private final Long startTime;

    public TradeHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.startTime = startTime;
    }

    public boolean getAgreementById(String id) {
        try {
            agreement = APIConnector.getAgreementById(EventHandler.getToken(), id);

        } catch (APIException e) {
            logger.debug("Unable to process trade event");
            return false;
        }

        return agreement != null;
    }

    public void postContractProposal(VenueTradeAgreement venueTradeAgreement, PartyRole botPartyRole) {
        TradeAgreement tradeAgreement = TradeService.buildTradeAgreement(venueTradeAgreement);
        postContractProposal(tradeAgreement, botPartyRole);
    }

    public void postContractProposal(TradeAgreement trade, PartyRole botPartyRole) {
        ContractProposal contractProposal = ContractService.createContractProposal(trade, botPartyRole);

        try {
            APIConnector.postContractProposal(EventHandler.getToken(), contractProposal);
        } catch (APIException e) {
            logger.debug("Unable to process trade event");
        }
    }

    public void run() {
        //Parse agreement id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String agreementId = arr[arr.length - 1];

        //Get agreement by id
        getAgreementById(agreementId);
        VenueTradeAgreement trade = agreement.getTrade();

        String botPartyId = configurator.getBotPartyId();

        Optional<TransactingParty> transactingPartyById = TradeService.getTransactingPartyById(trade, botPartyId);
        if (transactingPartyById.isEmpty()) {
            logger.info("Unable to propose contract due to error retrieving bot party id and/or bot party role");
            return;
        }

        PartyRole botPartyRole = transactingPartyById.get().getPartyRole();

        AgreementRule rule = configurator.getAgreementRules().getFirstApplicableRule(trade, botPartyId);
        if (rule != null && rule.shouldIgnore()) {
            return;
        }

        double delay = (rule == null) ? 0 : rule.getDelay();
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        postContractProposal(trade, botPartyRole);
    }
}
