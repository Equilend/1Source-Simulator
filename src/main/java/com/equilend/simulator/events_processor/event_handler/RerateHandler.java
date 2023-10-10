package com.equilend.simulator.events_processor.event_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.event.Event;
import com.equilend.simulator.rerate.Rerate;
import com.equilend.simulator.token.BearerToken;
import com.google.gson.Gson;

public class RerateHandler implements EventHandler {

    private Event event;
    private Configurator configurator;
    private String botPartyId;
    private Long startTime;
    private static final Logger logger = LogManager.getLogger();
    private Gson gson = new Gson();
    
    public RerateHandler(Event e, Configurator configurator, Long startTime){
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

    private Rerate getRerateById(String id){
        Rerate rerate = null;
        try {
            rerate = APIConnector.getRerateById(getToken(), id);
        }
        catch (APIException e){
            logger.debug("Unable to process rerate event");
        }
        return rerate;
    }

    private Contract getContractById(String id) {
        Contract contract = null;
        try {
            contract = APIConnector.getContractById(getToken(), id);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }   

        return contract;
    }   

    public void cancelRerateProposal(String contractId, String rerateId, Double delay){
        Long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis){
            Thread.yield();
        }

        try {
            APIConnector.cancelRerateProposal(getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }
    }

    public void approveRerateProposal(String contractId, String rerateId, Double delay){
        Long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis){
            Thread.yield();
        }   
        
        try {
            APIConnector.approveRerateProposal(getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }
    }    

    public void declineRerateProposal(String contractId, String rerateId, Double delay){
        Long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis){
            Thread.yield();
        }  
        
        try {
            APIConnector.declineRerateProposal(getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Unable to process rerate event");
        }        
    }     

    public void run(){
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String rerateId = arr[arr.length-1];
        Rerate rerate = getRerateById(rerateId);
        if (rerate == null) return;

        logger.info(gson.toJson(rerate));

        String contractId = rerate.getloanId();
        Contract contract = getContractById(contractId);

        // TODO: determine whether bot is rerate initiator or recipient
        // This flag will do for now..
        boolean cancelOrIgnoreMode = false; 
        if (cancelOrIgnoreMode){
            RerateCancelRule rule = configurator.getRerateRules().getCancelRule(rerate, contract, botPartyId);
            if (rule == null || !rule.shouldCancel()) return; //default to ignore/ not cancelling
            Double delay = rule.getDelay();
            cancelRerateProposal(contractId, rerateId, delay);
        }
        else{
            RerateApproveRule rule = configurator.getRerateRules().getApproveRule(rerate, contract, botPartyId);
            if (rule == null){
                //no applicable rule, default to approve w/o delay
                approveRerateProposal(contractId, rerateId, 0.0);
            }
            else if (rule.shouldApprove()){
                Double delay = rule.getDelay();
                approveRerateProposal(contractId, rerateId, delay);
            }
            else{
                Double delay = rule.getDelay();
                declineRerateProposal(contractId, rerateId, delay);
            }
        }        
    }
}