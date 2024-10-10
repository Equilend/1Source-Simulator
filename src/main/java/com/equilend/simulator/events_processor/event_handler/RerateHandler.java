package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.LoanService.getLoanById;
import static com.equilend.simulator.service.RerateService.getRerateById;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.ReratePendingCancelRule;
import com.equilend.simulator.rules_processor.RerateRuleProcessor;
import com.os.client.model.Event;
import com.os.client.model.Loan;
import com.os.client.model.Rerate;

public class RerateHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(RerateHandler.class.getName());
    private final Event event;
    private final Config config;
    private final String botPartyId;
    private final Long startTime;

    public RerateHandler(Event e, Config config, Long startTime) {
        this.event = e;
        this.config = config;
        this.botPartyId = config.getBotPartyId();
        this.startTime = startTime;
    }


    public void run() {
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        try {
            String rerateId = arr[arr.length - 1];
            Rerate rerate = getRerateById(rerateId);
            if (rerate == null) {
                return;
            }
            String loanId = rerate.getLoanId();
            Loan loan = getLoanById(loanId);
            switch (event.getEventType()) {
                case RERATE_PROPOSED:
                    RerateCancelRule cancelRule = config.getRerateRules().getCancelRule(rerate, loan, botPartyId);
                    if (cancelRule != null && cancelRule.shouldCancel()) {
                        RerateRuleProcessor.process(startTime, cancelRule, loan, rerate);
                        return;
                    }
                    RerateApproveRule approveRule = config.getRerateRules().getApproveRule(rerate, loan, botPartyId);
                    if (approveRule != null && !approveRule.shouldIgnore()) {
                        RerateRuleProcessor.process(startTime, approveRule, loan, rerate);
                        return;
                    }
                    logger.debug("Event {} with ResourceUri {} has not been processed by rules", event.getEventType(), event.getResourceUri());
                    break;
                case RERATE_PENDING:
                    ReratePendingCancelRule pendingCancelRule = config.getRerateRules()
                        .getPendingCancelRule(rerate, loan, botPartyId);
                    if (pendingCancelRule != null && pendingCancelRule.shouldCancel()) {
                        RerateRuleProcessor.process(startTime, pendingCancelRule, loan, rerate);
                        return;
                    }
                    logger.debug("Event {} with ResourceUri {} has not been processed by rules", event.getEventType(), event.getResourceUri());
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }

        } catch (APIException e) {
            logger.error("Unable to process rerate event", e);
        }
    }
}