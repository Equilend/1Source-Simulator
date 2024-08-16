package com.equilend.simulator.events_processor.event_handler;

import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.event.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyinsHandler {
    private static final Logger logger = LogManager.getLogger(BuyinsHandler.class.getName());
    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;

    public BuyinsHandler(Event event, Configurator configurator,Long startTime) {
        this.event = event;
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.startTime = startTime;
    }

    public void run() {
        //Parse contract id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String contractId = arr[arr.length - 1];

        //Get contract by Id
        Contract contract = getContractById(contractId);
        if (contract == null) {
            return;
        }
    }
}
