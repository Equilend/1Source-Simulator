package com.equilend.simulator.events_processor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.events_processor.event_handler.BuyinHandler;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.events_processor.event_handler.LoanHandler;
import com.equilend.simulator.events_processor.event_handler.RecallHandler;
import com.equilend.simulator.events_processor.event_handler.RerateHandler;
import com.equilend.simulator.events_processor.event_handler.ReturnsHandler;
import com.equilend.simulator.events_processor.event_handler.SplitHandler;
import com.equilend.simulator.events_processor.event_handler.TradeHandler;
import com.os.client.model.Event;
import com.os.client.model.EventType;

public class EventsProcessor implements Runnable {

    private static final Logger logger = LogManager.getLogger(EventsProcessor.class.getName());
    private final Config config;
    private final long waitInterval;


    public EventsProcessor() {
        this.config = Config.getInstance();
        this.waitInterval = config.getEventFetchIntervalMillis();
    }

    private static class EventHandlerThread implements ThreadFactory {

        private static int count = 0;

        public Thread newThread(Runnable r) {
            return new Thread(r, "Event-Handler-Thread-" + count++);
        }
    }

    public void run() {
        ExecutorService exec = Executors.newCachedThreadPool(new EventHandlerThread());

        if (config.isAnalysisModeEnable()) {
        	logger.info("Retrieving token");
        }
        
        OneSourceToken token;
        try {
            token = OneSourceToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to listen for new events due to error with token");
            return;
        }

        OffsetDateTime since = APIConnector.getCurrentTime().minusHours(3);
        Long fromEventId = null;

        while (true) {
            try {
                if (config.isAnalysisModeEnable()) {
                	logger.info("Sleeping for: " + waitInterval);
                }
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
                logger.debug("Unable to listen for new events due to thread sleep interruption", e);
                return;
            }

            List<Event> events;
            try {
                if (config.isAnalysisModeEnable()) {
                	logger.info("Polling for events from: " + fromEventId);
                }
                events = APIConnector.getAllEvents(token, since, fromEventId);
            } catch (APIException e) {
                logger.error("Unable to get new events", e);
                return;
            }

            if (events == null || events.isEmpty()) {
                continue; //Back to sleep
            }

            fromEventId = events.get(0).getEventId() + 1;

            for (Event event : events) {
                boolean shouldIgnore = config.getEventRules().shouldIgnoreEvent(event);
                if (shouldIgnore) {
                    logger.debug("Ignoring event of type {}", event.getEventType());
                    continue;
                } else {
                    logger.debug("Attempting to dispatch event of type {}", event.getEventType());
                }

                EventHandler task = null;
                EventType type = event.getEventType();

                if (config.isAnalysisModeEnable()) {
                	logger.info("Found " + type.toString() + " event");
                }

                switch (type) {
                    case TRADE_AGREED:
                        task = new TradeHandler(event, config, System.currentTimeMillis());
                        break;
                    case LOAN_OPENED:
                    case LOAN_PROPOSED:
                    case LOAN_PENDING:
                        task = new LoanHandler(event, config, System.currentTimeMillis());
                        break;
                    case RERATE_PROPOSED:
                    case RERATE_PENDING:
                        task = new RerateHandler(event, config, System.currentTimeMillis());
                        break;
                    case RETURN_PENDING:
                    case RETURN_ACKNOWLEDGED:
                        task = new ReturnsHandler(event, config, System.currentTimeMillis());
                        break;
                    case BUYIN_PENDING:
                        task = new BuyinHandler(event, config, System.currentTimeMillis());
                        break;
                    case RECALL_OPENED:
                    case RECALL_ACKNOWLEDGED:
                        task = new RecallHandler(event, config, System.currentTimeMillis());
                        break;
                    case LOAN_SPLIT_PROPOSED:
                        task = new SplitHandler(event, config, System.currentTimeMillis());
                        break;
                }

                if (task != null) {
                    exec.execute(task);
                }
            }
        }
    }

}