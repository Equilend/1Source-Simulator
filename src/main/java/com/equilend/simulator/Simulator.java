package com.equilend.simulator;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.events_processor.EventsProcessor;
import com.equilend.simulator.scheduler.Scheduler;
import com.equilend.simulator.token.BearerToken;


public class Simulator {   
    
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {  
        Configurator configurator = new Configurator();

        boolean useTestLenderAuth = configurator.getGeneralRules().getBotPartyId().equals("TLEN-US");
        logger.info("Bot Party ID: {}", configurator.getGeneralRules().getBotPartyId());
        if (useTestLenderAuth){
            BearerToken.configureToken(configurator.getAuthorizationRules().getLender());
        }else{
            BearerToken.configureToken(configurator.getAuthorizationRules().getBorrower());
        }

        ExecutorService execOutgoing = Executors.newSingleThreadExecutor();
        execOutgoing.execute(new Scheduler(configurator));

        // ExecutorService execIncoming = Executors.newSingleThreadExecutor();
        // execIncoming.execute(new EventsProcessor(configurator));
        
        System.out.println("Enter q to quit");
        Scanner input = new Scanner(System.in);
        String line;
        while (input.hasNext()){
            line = input.nextLine();
            if (line.equalsIgnoreCase("q")){
                System.out.println("You've pressed \'q\'");
                System.out.println("Let us clean up a lil and we'll be done, thank you for your patience...");
                execOutgoing.shutdownNow();
                // execIncoming.shutdownNow();
                break;
            }
        }
        input.close();

        logger.info("DONE :)");
    }

}