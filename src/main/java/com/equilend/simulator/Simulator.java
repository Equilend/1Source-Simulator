package com.equilend.simulator;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.events_processor.EventsProcessor;
import com.equilend.simulator.record_analyzer.RecordAnalyzer;
import com.equilend.simulator.scheduler.Scheduler;
import com.equilend.simulator.utils.PropertiesUtil;

public class Simulator {

    private static final String analyzeRecordsMsg = "\n" + //
        "     _                _                                            _     \n" + //
        "    / \\   _ __   __ _| |_   _ _______   _ __ ___  ___ ___  _ __ __| |___ \n" + //
        "   / _ \\ | '_ \\ / _` | | | | |_  / _ \\ | '__/ _ \\/ __/ _ \\| '__/ _` / __|\n" + //
        "  / ___ \\| | | | (_| | | |_| |/ /  __/ | | |  __/ (_| (_) | | | (_| \\__ \\\n" + //
        " /_/   \\_\\_| |_|\\__,_|_|\\__, /___\\___| |_|  \\___|\\___\\___/|_|  \\__,_|___/\n" + //
        "                        |___/                                            \n";
    private static final String processEventsMsg = "\n" + //
        "  ____                                                     _       \n" + //
        " |  _ \\ _ __ ___   ___ ___  ___ ___    _____   _____ _ __ | |_ ___ \n" + //
        " | |_) | '__/ _ \\ / __/ _ \\/ __/ __|  / _ \\ \\ / / _ \\ '_ \\| __/ __|\n" + //
        " |  __/| | | (_) | (_|  __/\\__ \\__ \\ |  __/\\ V /  __/ | | | |_\\__ \\\n" + //
        " |_|   |_|  \\___/ \\___\\___||___/___/  \\___| \\_/ \\___|_| |_|\\__|___/\n\n";

    public static void warmUp() {
        try {
            APIConnector.getContractById(OneSourceToken.getToken(), "DEAD-BEEF");
        } catch (APIException e) {
            System.out.println("Exception in warmUp(): " + e.getMessage());
        }
    }

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
        Configurator configurator = new Configurator(props);

        warmUp();

        String fence = "=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$=+=$";
        if (configurator.getRerateRules().getAnalysisMode() || configurator.getContractRules().getAnalysisMode()) {
            System.out.println(fence);
            System.out.println(analyzeRecordsMsg);
            System.out.println(fence);
            RecordAnalyzer analyzer = new RecordAnalyzer(configurator);
            analyzer.run();
        }
        ExecutorService execOutgoing;
        if (configurator.getContractRules().schedulerMode()) {
            System.out.println(fence);
            //
            //
            //
            //
            //
            String generateContractsMsg = "\n" + //
                "   ____                           _                         _                  _       \n" + //
                "  / ___| ___ _ __   ___ _ __ __ _| |_ ___    ___ ___  _ __ | |_ _ __ __ _  ___| |_ ___ \n" + //
                " | |  _ / _ \\ '_ \\ / _ \\ '__/ _` | __/ _ \\  / __/ _ \\| '_ \\| __| '__/ _` |/ __| __/ __|\n" + //
                " | |_| |  __/ | | |  __/ | | (_| | ||  __/ | (_| (_) | | | | |_| | | (_| | (__| |_\\__ \\\n" + //
                "  \\____|\\___|_| |_|\\___|_|  \\__,_|\\__\\___|  \\___\\___/|_| |_|\\__|_|  \\__,_|\\___|\\__|___/\n\n";
            System.out.println(generateContractsMsg);
            System.out.println(fence);
            execOutgoing = Executors.newSingleThreadExecutor(new SchedulerThread());
            execOutgoing.execute(new Scheduler(configurator));
        }

        ExecutorService execIncoming = Executors.newSingleThreadExecutor(new EventProcessorThread());
        execIncoming.execute(new EventsProcessor(configurator));
        System.out.println(fence);
        System.out.println(processEventsMsg);
        System.out.println(fence);

        while (true) {
            Thread.yield();
        }
    }
}