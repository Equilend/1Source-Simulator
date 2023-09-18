package com.equilend.simulator.events_processor.event_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.agreement.Agreement;
import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.ContractProposalResponse;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.contract.ContractProposal;
import com.equilend.simulator.event.Event;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.trade.Trade;

public class TradeHandler implements EventHandler {

    private Event event;
    private Agreement agreement;
    private Configurator rules;
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

    public TradeHandler(Event e, Configurator rules) {
        this.event = e;
        this.rules = rules;
    }

    public boolean getAgreementById(String id) {
        try {
            agreement = APIConnector.getAgreementById(getToken(), id);
        } catch (APIException e) {
            logger.error("Unable to process trade event");
            return false;
        }

        if (agreement == null) return false;
        return true;
    }

    public boolean postContractProposal(Trade trade) {
        ContractProposal contractProposal = ContractProposal.createContractProposal(trade);
    
        ContractProposalResponse response;
        try {
            response = APIConnector.postContractProposal(getToken(), contractProposal);
        } catch (APIException e) {
            logger.error("Unable to process trade event");
            return false;
        }
        
        logger.info("Proposing contract {}", response.getContractId());
        return true;
    }

    public void run() {
        //Parse agreement id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String agreementId = arr[arr.length-1];

        //Get agreement by id
        getAgreementById(agreementId);

        //Create contract and post proposal
        Trade trade = agreement.getTrade();
        if (rules.getAgreementRules().shouldIgnoreTrade(trade)){
            return;
        }
        postContractProposal(trade);
    }   

}