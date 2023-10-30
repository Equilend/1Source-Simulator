package com.equilend.simulator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.events_processor.EventsProcessor;
import com.equilend.simulator.record_analyzer.RecordAnalyzer;
import com.equilend.simulator.scheduler.Scheduler;

public class Simulator {   
    
    private static final Logger logger = LogManager.getLogger();

    public static void warmUp(){
        try{
            APIConnector.getContractById(OneSourceToken.getToken(),"DEAD-BEEF");
        }
        catch (APIException e){
            
        }
    }

    private static class EventProcessorThread implements ThreadFactory {
        public Thread newThread(Runnable r){
            return new Thread(r, "Events-Processor-Thread");
        }
    }    

    private static class SchedulerThread implements ThreadFactory {
        public Thread newThread(Runnable r){
            return new Thread(r, "Scheduler-Thread");
        }
    } 

    public static void main(String[] args) { 
        logger.info("\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$\n\n\n"
                    + "Starting Program..." + "\n\n\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$");
        Configurator configurator = new Configurator();

        warmUp();

        if (configurator.getRerateRules().getAnalysisMode() || configurator.getContractRules().getAnalysisMode()){
            logger.info("\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$\n\n\n"
                    + "Analyzing existing records" + "\n\n\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$");
            RecordAnalyzer analyzer = new RecordAnalyzer(configurator);
            analyzer.run();
        }
        ExecutorService execOutgoing = null; 
        if (configurator.getContractRules().schedulerMode()){
            logger.info("\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$\n\n\n" 
            + "Generating contracts from rules" + "\n\n\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$");

            execOutgoing = Executors.newSingleThreadExecutor(new SchedulerThread());
            execOutgoing.execute(new Scheduler(configurator));
        }
        
        ExecutorService execIncoming = Executors.newSingleThreadExecutor(new EventProcessorThread());
        execIncoming.execute(new EventsProcessor(configurator));
        logger.info("\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$\n\n\n" 
                    + "Now listening for events!" + "\n\n\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$");

        while (true){
            Thread.yield();
        }
    }

}