package com.equilend.simulator.events_processor.event_handler;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface EventHandler extends Runnable {

    Logger logger = LogManager.getLogger(EventHandler.class.getName());

    static OneSourceToken getToken() {
        OneSourceToken token;

        try {
            token = OneSourceToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to process loan event due to error with token");
            return null;
        }

        return token;
    }
}