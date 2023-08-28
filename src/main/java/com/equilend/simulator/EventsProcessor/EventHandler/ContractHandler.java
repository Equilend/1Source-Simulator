package com.equilend.simulator.EventsProcessor.EventHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.API.APIConnector;
import com.equilend.simulator.API.APIException;
import com.equilend.simulator.Configurator.Configurator;
import com.equilend.simulator.Contract.Contract;
import com.equilend.simulator.Contract.ContractProposal;
import com.equilend.simulator.Event.Event;
import com.equilend.simulator.Settlement.AcceptSettlement;
import com.equilend.simulator.Settlement.Settlement;
import com.equilend.simulator.Token.BearerToken;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class ContractHandler implements EventHandler {
    private Event event;
    private Contract contract;
    private Configurator rules;
    private static final Logger logger = LogManager.getLogger();
    
    public BearerToken getToken(){
        BearerToken token = null;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to process contract event");
            return null;
        }
        return token;
    }
    
    public ContractHandler(Event e, Configurator rules){
        this.event = e;
        this.rules = rules;
    }

    private boolean getContractById(String id){
        try {
            contract = APIConnector.getContractById(getToken(), id);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
            return false;
        }   
        if (contract == null) return false;

        return true;    
    }

    private boolean acceptContractProposal(String contractId) 
    {
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

    private boolean declineContractProposal(String contractId) 
    {
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
        if (!rules.ignoreProposal(contract)){
            if (rules.shouldAcceptProposal(contract)){
                acceptContractProposal(contractId); //returns true or false based on success
            }
            else{
                declineContractProposal(contractId); //returns true or false based on success
            }
        }
    }    
}
