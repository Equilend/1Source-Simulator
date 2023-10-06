package com.equilend.simulator.events_processor.event_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractResponsiveRule;
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
    private Long startTime;
    private static final Logger logger = LogManager.getLogger();
    
    public ContractHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.botPartyId = configurator.getGeneralRules().getBotPartyId();
        this.startTime = startTime;
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
            logger.debug("Unable to process contract event");
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

    private void cancelContractProposal(String contractId, Double delay) {
        Long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis){
            Thread.yield();
        }

        try {
            APIConnector.cancelContractProposal(getToken(), contractId);
        } catch (APIException e) {
            logger.debug("Unable to process contract event");
        }
    }    

    private void acceptContractProposal(String contractId, Double delay) {
        Settlement settlement = ContractProposal.createSettlement(PartyRole.BORROWER);
        AcceptSettlement acceptSettlement = new AcceptSettlement(settlement);
        
        Long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis){
            Thread.yield();
        }

        try {
            APIConnector.acceptContractProposal(getToken(), contractId, acceptSettlement);
        } catch (APIException e) {
            logger.debug("Unable to process contract event");
        }
    }

    private void declineContractProposal(String contractId, Double delay) {
        Long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis){
            Thread.yield();
        }

        try {
            APIConnector.declineContractProposal(getToken(), contractId);
        } catch (APIException e) {
            logger.debug("Unable to process contract event");
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
            Double delay = configurator.getContractRules().shouldIgnoreTrade(contract, partyId);
            if (delay == -1) return;  
            cancelContractProposal(contractId, delay);

        }
        else{
            //Analyze contract to decide whether to accept or decline based on configurator
            ContractResponsiveRule rule = configurator.getContractRules().getApproveOrRejectApplicableRule(contract, partyId);
            if (rule == null){
                //no applicable rule, default to approve w/o delay
                acceptContractProposal(contractId, 0.0);
            }
            else if(rule.isShouldApprove()){
                acceptContractProposal(contractId, rule.getDelay());
            }
            else{
                declineContractProposal(contractId, rule.getDelay());
            }

        }
    }
    
}