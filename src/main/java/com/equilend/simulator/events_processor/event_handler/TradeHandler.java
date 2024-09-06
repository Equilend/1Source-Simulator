package com.equilend.simulator.events_processor.event_handler;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementRule;
import com.equilend.simulator.model.agreement.Agreement;
import com.equilend.simulator.model.loan.LoanProposal;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.trade.TradeAgreement;
import com.equilend.simulator.model.venue.VenueTradeAgreement;
import com.equilend.simulator.service.LoanService;
import com.equilend.simulator.service.TradeService;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TradeHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(TradeHandler.class.getName());
    private final Event event;
    private Agreement agreement;
    private final Config config;
    private final Long startTime;

    public TradeHandler(Event e, Config config, Long startTime) {
        this.event = e;
        this.config = config;
        this.startTime = startTime;
    }

    public boolean getAgreementById(String id) {
        try {
            agreement = APIConnector.getAgreementById(EventHandler.getToken(), id);

        } catch (APIException e) {
            logger.debug("Unable to process trade event ", e);
            return false;
        }

        return agreement != null;
    }

    public void postLoanProposal(VenueTradeAgreement venueTradeAgreement, PartyRole botPartyRole) {
        TradeAgreement tradeAgreement = TradeService.buildTradeAgreement(venueTradeAgreement);
        postLoanProposal(tradeAgreement, botPartyRole);
    }

    public void postLoanProposal(TradeAgreement trade, PartyRole botPartyRole) {
        LoanProposal loanProposal = LoanService.createLoanProposal(trade, botPartyRole);

        try {
            APIConnector.postLoanProposal(EventHandler.getToken(), loanProposal);
        } catch (APIException e) {
            logger.debug("Unable to process trade event", e);
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

        String botPartyId = config.getBotPartyId();

        Optional<TransactingParty> transactingPartyById = TradeService.getTransactingPartyById(trade, botPartyId);
        if (transactingPartyById.isEmpty()) {
            logger.info("Unable to propose loan due to error retrieving bot party id and/or bot party role");
            return;
        }

        PartyRole botPartyRole = transactingPartyById.get().getPartyRole();

        AgreementRule rule = config.getAgreementRules().getFirstApplicableRule(trade, botPartyId);
        if (rule != null && rule.shouldIgnore()) {
            return;
        }

        double delay = (rule == null) ? 0 : rule.getDelay();
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        postLoanProposal(trade, botPartyRole);
    }
}
