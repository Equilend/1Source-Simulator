package com.equilend.simulator;

import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.events_processor.EventsProcessor;
import com.equilend.simulator.record_analyzer.RecordAnalyzer;
import com.equilend.simulator.scheduler.Scheduler;
import com.equilend.simulator.utils.PropertiesUtil;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Simulator {

    private static final Logger logger = LogManager.getLogger(Simulator.class.getName());

    private static class EventProcessorThread implements ThreadFactory {

        public Thread newThread(Runnable r) {
            return new Thread(r, "Events-Processor-Thread");
        }
    }

    private static class SchedulerThread implements ThreadFactory {

        public Thread newThread(Runnable r) {
            return new Thread(r, "Scheduler-Thread");
        }
    }

    public static void main(String[] args) {
        Properties props = PropertiesUtil.loadProperties(args);
        logger.info("Initializing Simulator...");
        Config config = Config.getInstance();
        config.init(props);

        if (config.isAnalysisModeEnable()) {
            logger.info("Start analysis mode");
            new RecordAnalyzer().run();
            logger.info("Finish analysis mode");
        }

        if (config.isScheduledProducerEnable()) {
            logger.info("Start scheduled producer");
            ExecutorService execOutgoing = Executors.newSingleThreadExecutor(new SchedulerThread());
            execOutgoing.execute(new Scheduler());
        }

        logger.info("Start event listener");
        ExecutorService execIncoming = Executors.newSingleThreadExecutor(new EventProcessorThread());
        execIncoming.execute(new EventsProcessor());

        while (true) {
            Thread.yield();
        }
    }
}