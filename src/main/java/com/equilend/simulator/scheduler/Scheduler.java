package com.equilend.simulator.scheduler;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.contract.ContractProposal;
import com.equilend.simulator.rules.ContractGenerativeRule;
import com.equilend.simulator.rules.ContractRule;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.trade.instrument.Instrument;
import com.equilend.simulator.trade.transacting_party.Party;

public class Scheduler implements Runnable {

    private Configurator configurator;
    private String botPartyId;
    private Map<String, Party> parties;
    private Map<String, Instrument> instruments;

    private static final Logger logger = LogManager.getLogger();


    public Scheduler(Configurator configurator){
        this.configurator = configurator;
        this.botPartyId = configurator.getGeneralRules().getBotPartyId();
        this.parties = configurator.getParties();
        this.instruments = configurator.getInstruments();
    }

    public void run(){
        logger.info("Scheduler starting up");

        BearerToken token;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to listen for new events due to error with token");
            return;
        }

        //get generative contract rules/instructions from configurator
        List<ContractRule> rules = configurator.getContractRules().getSchedulerRules();
        
        //for each instruction, create a thread that handles this task.
        for (ContractRule rule : rules){
            ContractGenerativeRule instruction = (ContractGenerativeRule) rule;
            for (String counterpartyId : instruction.getCounterparties()){
                for (String ticker : instruction.getSecurities()){
                    for (String quantityStr : instruction.getQuantities() ){
                        Long quantity;
                        try {
                            quantity = Long.parseLong(quantityStr);
                        }
                        catch (NumberFormatException e){
                            //deal with ranges... random number b/w the range
                            //for now, just default to 500
                            quantity = Long.valueOf(500);
                        }

                        ContractProposal proposal = ContractProposal.createContractProposal(parties.get(botPartyId), parties.get(counterpartyId), instruments.get(ticker), quantity);
                        try {
                            APIConnector.postContractProposal(token, proposal);
                        } catch (APIException e){
                            logger.error("Unable to execute scheduled create contract", e);
                        }
                    }
                }
            }

        }
    }
}