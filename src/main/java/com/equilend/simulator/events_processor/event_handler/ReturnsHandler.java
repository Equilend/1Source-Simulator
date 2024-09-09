package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.LoanService.getLoanById;
import static com.equilend.simulator.service.ReturnService.getReturnById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.return_rules.ReturnAcknowledgeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnCancelRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnSettlementStatusUpdateRule;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.returns.ModelReturn;
import com.equilend.simulator.rules_processor.ReturnRuleProcessor;
import com.equilend.simulator.service.LoanService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            boolean isInitiator = LoanService.isInitiator(loan, botPartyId);
            logger.debug("Is initiator?: " + isInitiator);
            switch (event.getEventType()) {
                case RETURN_PENDING:
                    if (!isInitiator) {
                        ReturnAcknowledgeRule acknowledgeRule = config.getReturnRules()
                            .getReturnAcknowledgeRule(oneSourceReturn, loan, botPartyId);
                        if (acknowledgeRule != null && !acknowledgeRule.isIgnored()) {
                            ReturnRuleProcessor.process(startTime, acknowledgeRule, loan, oneSourceReturn);
                            return;
                        }
                    }
                    break;
                case RETURN_ACKNOWLEDGED:
                    if (isInitiator) {
                        ReturnCancelRule cancelRule = config.getReturnRules()
                            .getReturnCancelRule(oneSourceReturn, loan, botPartyId);
                        if (cancelRule != null && !cancelRule.isIgnored()) {
                            ReturnRuleProcessor.process(startTime, cancelRule, loan, oneSourceReturn);
                            return;
                        }
                    }

                    ReturnSettlementStatusUpdateRule returnSettlementStatusUpdateRule = config.getReturnRules()
                        .getReturnSettlementStatusUpdateRule(oneSourceReturn, loan, botPartyId);
                    if (returnSettlementStatusUpdateRule != null && !returnSettlementStatusUpdateRule.isIgnored()) {
                        ReturnRuleProcessor.process(startTime, returnSettlementStatusUpdateRule, loan,
                            oneSourceReturn);
                        return;
                    }
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.error("Unable to process rerate event", e);
        }

    }

}