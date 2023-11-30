package com.equilend.simulator.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.contract.ContractProposal;
import com.equilend.simulator.model.trade.instrument.Instrument;
import com.equilend.simulator.model.trade.transacting_party.Party;
import com.equilend.simulator.model.trade.transacting_party.PartyRole;

public class ScheduledEventHandler implements Runnable {
    
    PartyRole botPartyRole;
    Party botParty;
    Party counterparty;
    Instrument security;
    String quantityStr;
    String idType;

    private static final Logger logger = LogManager.getLogger();

    public ScheduledEventHandler(PartyRole botPartyRole, Party botParty, Party counterparty, Instrument security, String quantityStr, String idType) {
        this.botPartyRole = botPartyRole; 
        this.botParty = botParty;
        this.counterparty = counterparty;
        this.security = security;
        this.quantityStr = quantityStr;
        this.idType = idType;
    }

    public void run() {
        OneSourceToken token;
        try {
            token = OneSourceToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to propose scheduled contract proposal due to error with token");
            return;
        }

        long quantity;
        try{
            quantity = Long.parseLong(quantityStr);
        } catch(NumberFormatException e){
            quantity = 666L;
        }

        try {
            ContractProposal proposal = ContractProposal.createContractProposal(botPartyRole, botParty, counterparty, security, quantity, idType);
            APIConnector.postContractProposal(token, proposal);
        } catch(APIException e){
            logger.info("Unable to propose scheduled contract proposal: " + e.getMessage());
        }
    }

}