package com.equilend.simulator.scheduler;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.contract.ContractProposal;
import com.equilend.simulator.model.party.Party;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.instrument.Instrument;
import com.equilend.simulator.service.ContractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScheduledEventHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ScheduledEventHandler.class.getName());
    PartyRole botPartyRole;
    Party botParty;
    Party counterparty;
    Instrument security;
    String quantityStr;
    String idType;


    public ScheduledEventHandler(PartyRole botPartyRole, Party botParty, Party counterparty, Instrument security,
        String quantityStr, String idType) {
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

        Integer quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            quantity = 666;
        }

        try {
            ContractProposal proposal = ContractService.createContractProposal(botPartyRole, botParty, counterparty,
                security, quantity, idType);
            APIConnector.postContractProposal(token, proposal);
        } catch (APIException e) {
            logger.info("Unable to propose scheduled contract proposal: " + e.getMessage());
        }
    }

}