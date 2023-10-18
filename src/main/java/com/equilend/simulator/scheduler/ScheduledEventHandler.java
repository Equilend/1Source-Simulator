package com.equilend.simulator.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.model.contract.ContractProposal;
import com.equilend.simulator.model.trade.instrument.Instrument;
import com.equilend.simulator.model.trade.transacting_party.Party;
import com.equilend.simulator.model.trade.transacting_party.PartyRole;
import com.equilend.simulator.token.BearerToken;

public class ScheduledEventHandler implements Runnable {
    
    PartyRole botPartyRole;
    Party botParty;
    Party counterparty;
    Instrument security;
    String quantityStr;

    private static final Logger logger = LogManager.getLogger();

    public ScheduledEventHandler(PartyRole botPartyRole, Party botParty, Party counterparty, Instrument security, String quantityStr) {
        this.botPartyRole = botPartyRole; 
        this.botParty = botParty;
        this.counterparty = counterparty;
        this.security = security;
        this.quantityStr = quantityStr;
    }

    public void run() {
        Long start = System.currentTimeMillis();
        BearerToken token;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to propose scheduled contract proposal due to error with token");
            return;
        }

        Long quantity;
        try{
            quantity = Long.parseLong(quantityStr);
        } catch(NumberFormatException e){
            quantity = 666L;
            //if quantityStr is a range, randomly select a number from that range
        }

        try {
            ContractProposal proposal = ContractProposal.createContractProposal(botPartyRole, botParty, counterparty, security, quantity);
            APIConnector.postContractProposal(token, proposal);
        } catch(APIException e){
            logger.info("Unable to propose scheduled contract proposal");
        }
        Long end = System.currentTimeMillis();
        logger.trace("EXECUTION TIME: {} milliseconds", end-start);
    }

}