package com.equilend.simulator.events_processor.event_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.agreement.Agreement;
import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementRule;
import com.equilend.simulator.contract.ContractProposal;
import com.equilend.simulator.event.Event;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.trade.Trade;
import com.equilend.simulator.trade.transacting_party.PartyRole;

public class TradeHandler implements EventHandler {

    private Event event;
    private Agreement agreement;
    private Configurator configurator;
    private Long startTime;
    private static final Logger logger = LogManager.getLogger();

    public BearerToken getToken() {
        BearerToken token = null;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to process trade event due to error with token");
            return null;
        }
        return token;
    }

    public TradeHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.startTime = startTime;
    }

    public boolean getAgreementById(String id) {
        try {
            agreement = APIConnector.getAgreementById(getToken(), id);
            
        } catch (APIException e) {
            logger.debug("Unable to process trade event");
            return false;
        }

        if (agreement == null) return false;
        return true;
    }

    public void postContractProposal(Trade trade, PartyRole botPartyRole) {
        ContractProposal contractProposal = ContractProposal.createContractProposal(trade, botPartyRole);
    
        try {
            APIConnector.postContractProposal(getToken(), contractProposal);
        } catch (APIException e) {
            logger.debug("Unable to process trade event");
        }
    }

    public void run() {
        //Parse agreement id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String agreementId = arr[arr.length-1];

        //Get agreement by id
        getAgreementById(agreementId);
        Trade trade = agreement.getTrade();

        String botPartyId = configurator.getGeneralRules().getBotPartyId();
        PartyRole botPartyRole = trade.getPartyRole(botPartyId);
        if (botPartyRole == null) {
            logger.info("Unable to propose contract due to error retrieving bot party id and/or bot party role");
            return;
        }

        AgreementRule rule = configurator.getAgreementRules().getFirstApplicableRule(trade, botPartyId);
        if (rule != null && rule.shouldIgnore()) return;

        Double delay = (rule == null) ? 0 : rule.getDelay();
        Long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis){
            Thread.yield();
        }
        postContractProposal(trade, botPartyRole);
    }   

}