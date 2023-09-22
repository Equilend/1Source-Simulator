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
import com.equilend.simulator.trade.transacting_party.TransactingParty;

public class ContractHandler implements EventHandler {

    private Event event;
    private Configurator configurator;
    private String botPartyId;
    private static final Logger logger = LogManager.getLogger();
    
    public ContractHandler(Event e, Configurator configurator) {
        this.event = e;
        this.configurator = configurator;
        this.botPartyId = configurator.getGeneralRules().getBotPartyId();
    }

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

    private Contract getContractById(String id) {
        Contract contract = null;
        try {
            contract = APIConnector.getContractById(getToken(), id);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
        }   

        return contract;
    }

    private boolean isBotLenderInContract(Contract contract){
        for (TransactingParty tp : contract.getTrade().getTransactingParties()){
            if (tp.getParty().getPartyId().equals(botPartyId)){
                return tp.getPartyRole() == PartyRole.LENDER;
            }
        }        
        return true;
    }

    private void cancelContractProposal(String contractId) {
        try {
            APIConnector.cancelContractProposal(getToken(), contractId);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
        }
    }    

    private void acceptContractProposal(String contractId) {
        Settlement settlement = ContractProposal.createSettlement(PartyRole.BORROWER);
        AcceptSettlement acceptSettlement = new AcceptSettlement(settlement);
        try {
            APIConnector.acceptContractProposal(getToken(), contractId, acceptSettlement);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
        }
    }

    private void declineContractProposal(String contractId) {
        try {
            APIConnector.declineContractProposal(getToken(), contractId);
        } catch (APIException e) {
            logger.error("Unable to process contract event");
        }
    }

    public void run(){
        //Parse contract id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String contractId = arr[arr.length-1];

        //Get contract by Id
        Contract contract = getContractById(contractId); //returns true or false based on success
        if (contract == null) return;

        String partyId = configurator.getGeneralRules().getBotPartyId();

        boolean botActAsLender = isBotLenderInContract(contract);
        if (botActAsLender){
            if (!configurator.getContractRules().shouldIgnoreTrade(contract, partyId)){
                cancelContractProposal(contractId);
            }
        }
        else{
            //Analyze contract to decide whether to accept or decline based on configurator
            if (configurator.getContractRules().shouldApproveTrade(contract, partyId)){
                acceptContractProposal(contractId);
            }
            else{
                declineContractProposal(contractId);
            }
        }
    }
    
}