package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.LoanService.getLoanById;
import static com.equilend.simulator.service.ReturnService.getReturnById;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.return_rules.ReturnAcknowledgeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnCancelRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnSettlementStatusUpdateRule;
import com.equilend.simulator.rules_processor.ReturnRuleProcessor;
import com.os.client.model.Event;
import com.os.client.model.Loan;
import com.os.client.model.ModelReturn;

public class ReturnsHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(ReturnsHandler.class.getName());
    private final Event event;
    private final Config config;
    private final String botPartyId;
    private final Long startTime;

    public ReturnsHandler(Event e, Config config, Long startTime) {
        this.event = e;
        this.config = config;
        this.botPartyId = config.getBotPartyId();
        this.startTime = startTime;
    }

    public void run() {
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String returnId = arr[arr.length - 1];
        try {
            ModelReturn oneSourceReturn = getReturnById(returnId);
            if (oneSourceReturn == null) {
                return;
            }
            Loan loan = getLoanById(oneSourceReturn.getLoanId());
            switch (event.getEventType()) {
                case RETURN_PENDING:
                    ReturnAcknowledgeRule acknowledgeRule = config.getReturnRules()
                        .getReturnAcknowledgeRule(oneSourceReturn, loan, botPartyId);
                    if (acknowledgeRule != null && !acknowledgeRule.isIgnored()) {
                        ReturnRuleProcessor.process(startTime, acknowledgeRule, loan, oneSourceReturn);
                        return;
                    }
                    logger.debug("Event {} with ResourceUri {} has not been processed by rules", event.getEventType(), event.getResourceUri());
                    break;
                case RETURN_ACKNOWLEDGED:
                    ReturnCancelRule cancelRule = config.getReturnRules()
                        .getReturnCancelRule(oneSourceReturn, loan, botPartyId);
                    if (cancelRule != null && cancelRule.shouldCancel()) {
                        ReturnRuleProcessor.process(startTime, cancelRule, loan, oneSourceReturn);
                        return;
                    }
                    ReturnSettlementStatusUpdateRule returnSettlementStatusUpdateRule = config.getReturnRules()
                        .getReturnSettlementStatusUpdateRule(oneSourceReturn, loan, botPartyId);
                    if (returnSettlementStatusUpdateRule != null && returnSettlementStatusUpdateRule.shouldUpdateSettlementStatus()) {
                        ReturnRuleProcessor.process(startTime, returnSettlementStatusUpdateRule, loan,
                            oneSourceReturn);
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