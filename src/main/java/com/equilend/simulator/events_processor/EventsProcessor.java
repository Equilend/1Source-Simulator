package com.equilend.simulator.events_processor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.event.Event;
import com.equilend.simulator.events_processor.event_handler.ContractHandler;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.events_processor.event_handler.TradeHandler;
import com.equilend.simulator.token.BearerToken;
import com.equilend.simulator.trade.transacting_party.PartyRole;

public class EventsProcessor implements Runnable {

    private Configurator configurator;
    private PartyRole mode;
    private Long waitInterval;
    private static final Logger logger = LogManager.getLogger();

    public EventsProcessor(Configurator configurator) {
        this.configurator = configurator;
        this.mode = configurator.getMode();
        this.waitInterval = configurator.getWaitIntervalMillis();
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
                logger.error("Unable to listen for new events", e);
                return;
            }
            logger.info("Retrieve events from event id {}", fromEventId);

            List<Event> events;
            try{
                events = APIConnector.getAllEvents(token, since, fromEventId);
            } catch(APIException e){
                logger.error("Unable to listen for new events", e);
                return;
            }

            if (events.size() == 0){
                logger.info("No new events");
                continue; //Back to sleep
            }
                
            fromEventId = events.get(0).getEventId() + 1;
            logger.info("Number of events retrieved {}", events.size());

            for (Event event : events){
                EventHandler task = null;
                switch (event.getEventType()){
                    case "TRADE":   
                        task = (mode == PartyRole.LENDER) ? new TradeHandler(event, configurator) : null;
                        break;
                    case "CONTRACT":    
                        task = (mode == PartyRole.BORROWER) ? new ContractHandler(event, configurator) : null;                           
                        break;
                }

                if (task != null) 
                    exec.execute(task);
            }
        } 
    }
        
}