package com.equilend.simulator.EventsProcessor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.API.APIConnector;
import com.equilend.simulator.API.APIException;
import com.equilend.simulator.Configurator.Configurator;
import com.equilend.simulator.Event.Event;
import com.equilend.simulator.EventsProcessor.EventHandler.ContractHandler;
import com.equilend.simulator.EventsProcessor.EventHandler.EventHandler;
import com.equilend.simulator.EventsProcessor.EventHandler.TradeHandler;
import com.equilend.simulator.Token.BearerToken;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class EventsProcessor implements Runnable{
    private Configurator configurator;
    private PartyRole mode;
    private Long waitInterval;
    private static final Logger logger = LogManager.getLogger();

    public EventsProcessor(Configurator configurator){
        this.configurator = configurator;
        this.mode = configurator.getMode();
        this.waitInterval = configurator.getWaitIntervalMillis();
    }

    public void run(){
        ExecutorService exec = Executors.newCachedThreadPool();

        BearerToken token;
        try {
            token = BearerToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to listen for new events");
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
                continue;
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
