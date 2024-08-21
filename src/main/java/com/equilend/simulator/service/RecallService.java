package com.equilend.simulator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.os.client.model.Recall;

public class RecallService {

    private static final Logger logger = LogManager.getLogger(RecallService.class.getName());

    public static Recall getRecallById(String recallId) throws APIException {
        return APIConnector.getRecallById(EventHandler.getToken(), recallId);
    }
}
