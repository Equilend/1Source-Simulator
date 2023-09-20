package com.equilend.simulator.scheduler;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.trade.instrument.Instrument;

public class Scheduler implements Runnable {

    private Configurator configurator;
    private static final Logger logger = LogManager.getLogger();


    public Scheduler(Configurator configurator){
        this.configurator = configurator;
    }

    private Instrument getRandomInstrument(){
        List<Instrument> instruments = configurator.getInstruments();
        int size = instruments.size();
        
        Random rand = new Random();
        return instruments.get(rand.nextInt(size));
    }

    public void run(){
        getRandomInstrument();
        logger.info("Scheduler starting up");

        //get generative contract rules/instructions from configurator
        
        //for each instruction, create a thread that handles this task.
    }
}