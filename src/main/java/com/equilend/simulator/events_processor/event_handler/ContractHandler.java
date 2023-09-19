package com.equilend.simulator.events_processor.event_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.contract.ContractProposal;
import com.equilend.simulator.event.Event;
import com.equilend.simulator.settlement.AcceptSettlement;
import com.equilend.simulator.settlement.Settlement;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.trade.transacting_party.PartyRole;

public class ContractHandler implements EventHandler {

    private Event event;
    private Contract contract;
    private Configurator rules;
    private static final Logger logger = LogManager.getLogger();
    
    public BearerToken getToken() {
        BearerToken token = null;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to process contract event due to error with token");
            return null;
        }

        return token;
    }
    
    public ContractHandler(Event e, Configurator rules) {
        this.event = e;
        this.rules = rules;
    }

    private boolean getContractById(String id) {
        try {
            contract = APIConnector.getContractById(getToken(), id);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
            return false;
        }   

        if (contract == null) return false;
        return true;    
    }

    private boolean acceptContractProposal(String contractId) {
        Settlement settlement = ContractProposal.createSettlement(PartyRole.BORROWER);
        AcceptSettlement acceptSettlement = new AcceptSettlement(settlement);
        try {
            APIConnector.acceptContractProposal(getToken(), contractId, acceptSettlement);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
            return false;
        }

        logger.info("Accepting contract {}", contractId);
        return true;
    }

    private boolean declineContractProposal(String contractId) {
        try {
            APIConnector.declineContractProposal(getToken(), contractId);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
            return false;
        }

        logger.info("Declining contract {}", contractId);
        return true;
    }

    public void run(){
        //Parse contract id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String contractId = arr[arr.length-1];

        //Get contract by Id
        getContractById(contractId); //returns true or false based on success

        //Analyze contract to decide whether to accept or decline based on rules
        if (rules.getContractRules().shouldRejectTrade(contract)){
            declineContractProposal(contractId);
        }
        else{
            acceptContractProposal(contractId);
        }
    }
    
}