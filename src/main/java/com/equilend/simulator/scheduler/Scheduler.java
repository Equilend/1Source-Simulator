package com.equilend.simulator.scheduler;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.contract.ContractProposal;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.trade.instrument.Instrument;

public class Scheduler implements Runnable {

    private Configurator configurator;
    private Long waitInterval;
    private static final Logger logger = LogManager.getLogger();


    public Scheduler(Configurator configurator){
        this.configurator = configurator;
        this.waitInterval = configurator.getWaitIntervalMillis();
    }

    private Instrument getRandomInstrument(){
        List<Instrument> instruments = configurator.getInstruments();
        int size = instruments.size();
        
        Random rand = new Random();
        return instruments.get(rand.nextInt(size));
    }

    public void run(){
        BearerToken token;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to listen for new events due to error with token");
            return;
        }

        while (true){
            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
                logger.error("Unable to listen for new events", e);
                return;
            }
            
            Instrument instrument = getRandomInstrument();
            try {
                ContractProposal proposal = ContractProposal.createContractProposal(instrument);
                APIConnector.postContractProposal(token, proposal);
            } catch (APIException e){
                logger.error("Error posting contract from scheduler", e);
            }

        }

    }
}