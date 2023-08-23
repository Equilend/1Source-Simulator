package com.equilend.simulator.EventsProcessor.EventHandler;

import com.equilend.simulator.API.APIConnector;
import com.equilend.simulator.API.APIException;
import com.equilend.simulator.Agreement.Agreement;
import com.equilend.simulator.Configurator.Configurator;
import com.equilend.simulator.Contract.ContractProposal;
import com.equilend.simulator.Event.Event;
import com.equilend.simulator.Token.BearerToken;

public class TradeHandler implements EventHandler{
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

    public TradeHandler(Event e, Configurator rules){
        this.event = e;
        this.rules = rules;
    }

    public void run(){
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String agreementId = arr[arr.length-1];

        Agreement agreement;
        try {
            agreement = APIConnector.getAgreementById(getToken(), agreementId);
        } catch (APIException e) {
            return;
        }
        if (agreement == null){
            return;
        }    

        if (rules.actOnTrade(agreement.getTrade())){
            ContractProposal contractProposal = ContractProposal.createContractProposal(agreement.getTrade());
        
            try {
                APIConnector.postContractProposal(getToken(), contractProposal);
            } catch (APIException e) {
                return;
            }
        }
        

    }    
}
