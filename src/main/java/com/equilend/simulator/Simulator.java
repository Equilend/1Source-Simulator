package com.equilend.simulator;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.DatalendAPIConnector;
import com.equilend.simulator.auth.DatalendToken;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.events_processor.EventsProcessor;
import com.equilend.simulator.record_analyzer.RecordAnalyzer;
import com.equilend.simulator.scheduler.Scheduler;

public class Simulator {   
    
    private static final Logger logger = LogManager.getLogger();

    public static void testDatalend(){
        try {
            List<String> tickers = Arrays.asList("MSFT", "AAPL", "AMZN", "GOOG", "META",
                                                "NVDA", "TSLA", "IBM", "BABA", "CRM");
            for (String ticker : tickers){
                Double price = DatalendAPIConnector.getSecurityPrice(DatalendToken.getToken(), "ticker", ticker);
                Double fee = DatalendAPIConnector.getSecurityFee(DatalendToken.getToken(), "ticker", ticker);
                Double rebate = DatalendAPIConnector.getSecurityRebate(DatalendToken.getToken(), "ticker", ticker);
                logger.info("{}: Price ${}, Avg Fee {}, Avg Rebate {}", ticker, price, fee, rebate);
            }
            
        } catch (APIException e) {
            logger.error(e);
        }
    }

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
        logger.info("=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$\n\n\n"
                    + "Starting Program..." + "\n\n\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$");
        Configurator configurator = new Configurator();

        warmUp();

        if (configurator.getRerateRules().getAnalysisMode() || configurator.getContractRules().getAnalysisMode()){
            logger.info("=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$\n\n\n"
                    + "Analyzing existing records" + "\n\n\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$");
            RecordAnalyzer analyzer = new RecordAnalyzer(configurator);
            analyzer.run();
        }
        logger.info("=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$\n\n\n" 
                    + "And we're live!!!" + "\n\n\n=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$");
        ExecutorService execOutgoing = null; 
        if (configurator.getContractRules().schedulerMode()){
            execOutgoing = Executors.newSingleThreadExecutor(new SchedulerThread());
            execOutgoing.execute(new Scheduler(configurator));
        }

        ExecutorService execIncoming = Executors.newSingleThreadExecutor(new EventProcessorThread());
        execIncoming.execute(new EventsProcessor(configurator));
        
        System.out.println("Enter q to quit");
        Scanner input = new Scanner(System.in);
        String line;
        while (input.hasNext()){
            line = input.nextLine();
            if (line.equalsIgnoreCase("q")){
                System.out.println("You've pressed \'q\'");
                System.out.println("Let us clean up a lil and we'll be done, thank you for your patience...");
                if (configurator.getContractRules().schedulerMode()) {
                    execOutgoing.shutdownNow();
                }
                execIncoming.shutdownNow();
                break;
            }
        }
        input.close();
        logger.info("DONE :)");
    }

}