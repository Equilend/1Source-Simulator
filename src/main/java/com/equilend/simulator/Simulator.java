package com.equilend.simulator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Simulator 
{   
    private static final Logger logger = LogManager.getLogger();
    
    public static void main(String[] args)  
    {
        Configurator configurator = new Configurator();
        //use configurator to set up Token class..

        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(new EventsProcessor(configurator));

        logger.info("DONE :)");
 
    }
}
