package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.recall.Recall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecallService {

    private static final Logger logger = LogManager.getLogger(RecallService.class.getName());

    public static Recall getRecallById(String recallId) throws APIException {
        return APIConnector.getRecallById(EventHandler.getToken(), recallId);
    }
}
