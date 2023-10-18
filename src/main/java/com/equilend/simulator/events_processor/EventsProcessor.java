package com.equilend.simulator.events_processor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.events_processor.event_handler.ContractHandler;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.events_processor.event_handler.RerateHandler;
import com.equilend.simulator.events_processor.event_handler.TradeHandler;
import com.equilend.simulator.token.BearerToken;

public class EventsProcessor implements Runnable {

    private Configurator configurator;
    private long waitInterval;
    private static final Logger logger = LogManager.getLogger();

    public EventsProcessor(Configurator configurator) {
        this.configurator = configurator;
        this.waitInterval = Long.valueOf(configurator.getGeneralRules().getEventFetchIntervalMillis());
    }

    public void run() {
        ExecutorService exec = Executors.newCachedThreadPool();

        BearerToken token;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to listen for new events due to error with token");
            return;
        }

        OffsetDateTime since = APIConnector.getCurrentTime();
        int fromEventId = 0;
        while (true){
            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
                logger.debug("Unable to listen for new events due to thread sleep interruption", e);
                break;
            }

            List<Event> events;
            try{
                events = APIConnector.getAllEvents(token, since, fromEventId);
            } catch(APIException e){
                logger.error("Unable to get new events", e);
                break;
            }

            if (events == null || events.size() == 0){
                continue; //Back to sleep
            }
                
            fromEventId = events.get(0).getEventId() + 1;

            for (Event event : events){
                boolean shouldIgnore = configurator.getEventRules().shouldIgnoreEvent(event);
                if (shouldIgnore){
                    logger.debug("Ignoring event of type {}", event.getEventType());
                    continue;
                }
                else{
                    logger.debug("Attempting to dispatch event of type {}", event.getEventType());
                }
                
                EventHandler task = null;
                String type = event.getEventType();
                switch (type){
                    case "TRADE":   
                        task = new TradeHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case "CONTRACT":  
                        task = new ContractHandler(event, configurator, System.currentTimeMillis());                           
                        break;
                    case "CONTRACT_APPROVE":
                        task = new RerateHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case "RERATE":
                        task = new RerateHandler(event, configurator, System.currentTimeMillis());
                        break;
                }

                if (task != null) 
                    exec.execute(task);
            }
        } 
    }
        
}