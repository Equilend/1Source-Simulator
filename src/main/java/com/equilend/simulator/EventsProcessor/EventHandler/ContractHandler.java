package com.equilend.simulator.EventsProcessor.EventHandler;

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
    private Configurator rules;
    
    public BearerToken getToken(){
        BearerToken token = null;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            e.printStackTrace();
        }
        return token;
    }
    
    public ContractHandler(Event e, Configurator rules){
        this.event = e;
        this.rules = rules;
    }

    private boolean acceptContractProposal(String contractId) 
    {
        Settlement settlement = ContractProposal.createSettlement(PartyRole.BORROWER);
        AcceptSettlement acceptSettlement = new AcceptSettlement(settlement);
        try {
            APIConnector.acceptContractProposal(getToken(), contractId, acceptSettlement);
        } catch (APIException e) {
            return false;
        }
        return true;
    }

    private boolean declineContractProposal(String contractId) 
    {
        try {
            APIConnector.declineContractProposal(getToken(), contractId);
        } catch (APIException e) {
            return false;
        }
        return true;
    }

    public void run(){
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String contractId = arr[arr.length-1];

        //Get contract by Id
        Contract contract;
        try {
            contract = APIConnector.getContractById(getToken(), contractId);
        } catch (APIException e) {
            e.printStackTrace();
            return;
        }

        //Analyze contract to decide whether to accept or decline based on rules

        if (!rules.ignoreProposal(contract)){
            if (rules.shouldAcceptProposal(contract)){
                acceptContractProposal(contractId);
            }
            else{
                declineContractProposal(contractId);
            }
        }
    }    
}
