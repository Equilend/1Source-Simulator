package com.equilend.simulator.events_processor;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.events_processor.event_handler.BuyinHandler;
import com.equilend.simulator.events_processor.event_handler.ContractHandler;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.events_processor.event_handler.RecallHandler;
import com.equilend.simulator.events_processor.event_handler.RerateHandler;
import com.equilend.simulator.events_processor.event_handler.ReturnsHandler;
import com.equilend.simulator.events_processor.event_handler.SplitHandler;
import com.equilend.simulator.events_processor.event_handler.TradeHandler;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.event.EventType;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventsProcessor implements Runnable {

    private static final Logger logger = LogManager.getLogger(EventsProcessor.class.getName());
    private final Configurator configurator;
    private final long waitInterval;


    public EventsProcessor(Configurator configurator) {
        this.configurator = configurator;
        this.waitInterval = configurator.getEventFetchIntervalMillis();
    }

    private static class EventHandlerThread implements ThreadFactory {

        private static int count = 0;

        public Thread newThread(Runnable r) {
            return new Thread(r, "Event-Handler-Thread-" + count++);
        }
    }

    public void run() {
        ExecutorService exec = Executors.newCachedThreadPool(new EventHandlerThread());

        OneSourceToken token;
        try {
            token = OneSourceToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to listen for new events due to error with token");
            return;
        }

        OffsetDateTime since = APIConnector.getCurrentTime();
        BigInteger fromEventId = null;

        while (true) {
            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
                logger.debug("Unable to listen for new events due to thread sleep interruption", e);
                return;
            }

            List<Event> events;
            try {
                events = APIConnector.getAllEvents(token, since, fromEventId);
            } catch (APIException e) {
                logger.error("Unable to get new events", e);
                return;
            }

            if (events == null || events.isEmpty()) {
                continue; //Back to sleep
            }

            fromEventId = new BigInteger(events.get(0).getEventId()).add(BigInteger.ONE);

            for (Event event : events) {
                boolean shouldIgnore = configurator.getEventRules().shouldIgnoreEvent(event);
                if (shouldIgnore) {
                    logger.debug("Ignoring event of type {}", event.getEventType());
                    continue;
                } else {
                    logger.debug("Attempting to dispatch event of type {}", event.getEventType());
                }

                EventHandler task = null;
                EventType type = event.getEventType();
                switch (type) {
                    case TRADE_AGREED:
                        task = new TradeHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case CONTRACT_OPENED:
                    case CONTRACT_PROPOSED:
                    case CONTRACT_PENDING:
                        task = new ContractHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case RERATE_PROPOSED:
                    case RERATE_PENDING:
                        task = new RerateHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case RETURN_PENDING:
                        task = new ReturnsHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case BUYIN_PENDING:
                        task = new BuyinHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case RECALL_OPENED:
                        task = new RecallHandler(event, configurator, System.currentTimeMillis());
                        break;
                    case CONTRACT_SPLIT_PROPOSED:
                        task = new SplitHandler(event, configurator, System.currentTimeMillis());
                        break;
                }

                if (task != null) {
                    exec.execute(task);
                }
            }
        }
    }

}