package com.equilend.simulator.events_processor.event_handler;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementProposeRule;
import com.equilend.simulator.model.agreement.Agreement;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.rules_processor.TradeRuleProcessor;
import com.equilend.simulator.service.AgreementService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TradeHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(TradeHandler.class.getName());
    private final Event event;
    private final Config config;
    private final String botPartyId;
    private final Long startTime;

    public TradeHandler(Event e, Config config, Long startTime) {
        this.event = e;
        this.config = config;
        this.botPartyId = config.getBotPartyId();
        this.startTime = startTime;
    }

    public void run() {
        //Parse agreement id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String agreementId = arr[arr.length - 1];

        try {
            Agreement agreement = AgreementService.getAgreementById(agreementId);
            switch (event.getEventType()) {
                case TRADE_AGREED:
                    AgreementProposeRule agreementProposeRule = config.getAgreementRules()
                        .getAgreementProposeRule(agreement.getTrade(), botPartyId);
                    if (agreementProposeRule != null && !agreementProposeRule.shouldIgnore()) {
                        TradeRuleProcessor.process(startTime, agreementProposeRule, agreement);
                    }
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.error("Unable to process trade event", e);
        }
    }
}
