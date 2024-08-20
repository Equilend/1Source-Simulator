package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.BuyinService.getBuyinById;
import static com.equilend.simulator.service.ContractService.getContractById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinAcceptRule;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.rules_processor.BuyinRuleProcessor;
import com.equilend.simulator.service.ContractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyinHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(BuyinHandler.class.getName());
    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;

    public BuyinHandler(Event event, Configurator configurator, Long startTime) {
        this.event = event;
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.startTime = startTime;
    }

    public void run() {
        //Parse contract id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String buyinId = arr[arr.length - 1];

        try {
            BuyinComplete buyin = getBuyinById(buyinId);
            if (buyin == null) {
                logger.warn("Buyin with id " + buyinId + " not found");
                return;
            }
            Contract contract = getContractById(buyin.getContractId());
            if (contract == null) {
                logger.warn("Contract with id " + buyin.getContractId() + " not found");
                return;
            }
            boolean isInitiator = ContractService.isInitiator(contract, botPartyId);
            switch (event.getEventType()) {
                case BUYIN_PENDING:
                    BuyinAcceptRule buyinAcceptRule = configurator.getBuyinRules()
                        .getBuyinAcceptRule(buyin, contract, botPartyId);
                    if (!isInitiator && buyinAcceptRule != null && buyinAcceptRule.shouldAccept()) {
                        BuyinRuleProcessor.process(startTime, buyinAcceptRule, contract, buyin);
                        return;
                    }
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.error("Unable to process buyin event", e);
        }
    }
}
