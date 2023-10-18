package com.equilend.simulator.events_processor.event_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.contract.ContractProposal;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.settlement.AcceptSettlement;
import com.equilend.simulator.model.settlement.Settlement;
import com.equilend.simulator.model.trade.transacting_party.PartyRole;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractResponsiveRule;
import com.equilend.simulator.token.OneSourceToken;

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

    public OneSourceToken getToken() {
        OneSourceToken token = null;
        try {
            token = OneSourceToken.getToken();
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

    private boolean didBotInitiate(Contract contract){
        // Currently, lender only provides its settlement info it only has lender settlement on contract
        //But borrower creates both lender and borrower settlement, even if lender is empty
        boolean lenderInitiated = contract.getSettlement().size() == 1;
        if (lenderInitiated){
            return contract.getTrade().getPartyRole(botPartyId) == PartyRole.LENDER;
        }
        //Of course, this won't work if lender provides both its own and the borrower's settlement info
        //But this is the best we can do until initiator party id given in contract json
        return contract.getTrade().getPartyRole(botPartyId) == PartyRole.BORROWER;
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
        Contract contract = getContractById(contractId);
        if (contract == null) return;

        boolean botInitiated = didBotInitiate(contract);
        if (botInitiated){
            Double delay = configurator.getContractRules().shouldIgnoreTrade(contract, botPartyId);
            if (delay == -1) return;  
            cancelContractProposal(contractId, delay);
        }
        else{
            //Analyze contract to decide whether to accept or decline based on configurator
            ContractResponsiveRule rule = configurator.getContractRules().getApproveOrRejectApplicableRule(contract, botPartyId);
            if (rule == null){
                //If no applicable rule, then default to ignoring the contract.
                return;
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