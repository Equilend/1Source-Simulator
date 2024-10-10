package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.os.client.model.Agreement;

public class AgreementService {
    public static Agreement getAgreementById(String id) throws APIException {
        return APIConnector.getAgreementById(EventHandler.getToken(), id);
    }
}
