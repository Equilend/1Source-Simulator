package com.equilend.simulator.events_processor.event_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;

public interface EventHandler extends Runnable {

    public static final Logger logger = LogManager.getLogger();

    public static OneSourceToken getToken() {
        OneSourceToken token = null;
        try {
            token = OneSourceToken.getToken();
        } catch (APIException e) {
            logger.error("Unable to process contract event due to error with token");
            return null;
        }

        return token;
    }

}