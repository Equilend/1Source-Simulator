package com.equilend.simulator;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.Configurator.Configurator;
import com.equilend.simulator.EventsProcessor.EventsProcessor;
import com.equilend.simulator.Token.BearerToken;


public class Simulator 
{   
    private static final Logger logger = LogManager.getLogger();
    
    public static void main(String[] args)  
    {
        Configurator configurator = new Configurator();
        
        //use configurator to set up Token class..
        BearerToken.configureToken(configurator.getLoginMap());
        
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(new EventsProcessor(configurator));
        
        System.out.println("Enter q to quit");
        Scanner input = new Scanner(System.in);
        String i;
        while (input.hasNext()){
            i = input.nextLine();
            if (i.equalsIgnoreCase("q")){
                System.out.println("You've pressed \'q\'");
                System.out.println("Let us clean up a lil and we'll be done in 5 secs at MOST...");
                exec.shutdownNow();
                break;
            }
        }
        input.close();

        logger.info("DONE :)");
 
    }
}
