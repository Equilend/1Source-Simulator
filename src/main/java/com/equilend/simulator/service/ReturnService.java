package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.returns.ModelReturn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReturnService {

    private static final Logger logger = LogManager.getLogger(ReturnService.class.getName());

    public static ModelReturn getReturnById(String returnId) throws APIException {
        return APIConnector.getReturnById(EventHandler.getToken(), returnId);
    }

}
