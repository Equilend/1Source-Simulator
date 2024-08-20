package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.ContractService.getContractById;
import static com.equilend.simulator.service.RecallService.getRecallById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.recall.Recall;
import com.equilend.simulator.service.ContractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecallHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(RecallHandler.class.getName());
    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;

    public RecallHandler(Event event, Configurator configurator, Long startTime) {
        this.event = event;
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.startTime = startTime;
    }

    @Override
    public void run() {
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String recallId = arr[arr.length - 1];
        try {
            Recall recall = getRecallById(recallId);
            if (recall == null) {
                return;
            }
            Contract contract = getContractById(recall.getContractId());
            boolean isInitiator = ContractService.isInitiator(contract, botPartyId);

        } catch (APIException e) {
            logger.error("Unable to process recall event", e);
        }
    }
}
