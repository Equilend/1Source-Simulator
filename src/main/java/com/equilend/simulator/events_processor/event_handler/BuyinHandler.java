package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.BuyinService.getBuyinById;
import static com.equilend.simulator.service.LoanService.getLoanById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinAcceptRule;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.rules_processor.BuyinRuleProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyinHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(BuyinHandler.class.getName());
    private final Event event;
    private final Config config;
    private final String botPartyId;
    private final Long startTime;

    public BuyinHandler(Event event, Config config, Long startTime) {
        this.event = event;
        this.config = config;
        this.botPartyId = config.getBotPartyId();
        this.startTime = startTime;
    }

    public void run() {
        //Parse loan id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String buyinId = arr[arr.length - 1];

        try {
            BuyinComplete buyin = getBuyinById(buyinId);
            if (buyin == null) {
                logger.warn("Buyin with id " + buyinId + " not found");
                return;
            }
            Loan loan = getLoanById(buyin.getLoanId());
            if (loan == null) {
                logger.warn("Loan with id " + buyin.getLoanId() + " not found");
                return;
            }
            switch (event.getEventType()) {
                case BUYIN_PENDING:
                    BuyinAcceptRule buyinAcceptRule = config.getBuyinRules()
                        .getBuyinAcceptRule(buyin, loan, botPartyId);
                    if (buyinAcceptRule != null && buyinAcceptRule.shouldAccept()) {
                        BuyinRuleProcessor.process(startTime, buyinAcceptRule, loan, buyin);
                        return;
                    }
                    logger.debug("Event {} with ResourceUri {} has not been processed by rules", event.getEventType(), event.getResourceUri());
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.error("Unable to process buyin event", e);
        }
    }
}
