package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.LoanService.getLoanById;
import static com.equilend.simulator.service.RecallService.getRecallById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinProposeRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallCancelRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeFromRecallRule;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.recall.Recall;
import com.equilend.simulator.rules_processor.BuyinRuleProcessor;
import com.equilend.simulator.rules_processor.RecallRuleProcessor;
import com.equilend.simulator.rules_processor.ReturnRuleProcessor;
import com.equilend.simulator.service.LoanService;
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
            Loan loan = getLoanById(recall.getLoanId());
            boolean isInitiator = LoanService.isInitiator(loan, botPartyId);
            switch (event.getEventType()) {
                case RECALL_OPENED:
                    if (isInitiator) {
                        BuyinProposeRule buyinProposeRule = configurator.getBuyinRules()
                            .getBuyinProposeRule(loan, botPartyId);
                        if (buyinProposeRule != null && buyinProposeRule.shouldSubmit()) {
                            BuyinRuleProcessor.process(startTime, buyinProposeRule, loan, null);
                            return;
                        }

                        ReturnProposeFromRecallRule returnProposeRule = configurator.getReturnRules()
                            .getReturnProposeFromRecallRule(loan, botPartyId);
                        if (returnProposeRule != null && returnProposeRule.shouldPropose()) {
                            ReturnRuleProcessor.process(startTime, returnProposeRule, loan, null);
                            return;
                        }

                        RecallCancelRule recallCancelRule = configurator.getRecallRules().getRecallCancelRule(recall,
                            loan, botPartyId);
                        if (recallCancelRule != null && recallCancelRule.shouldCancel()) {
                            RecallRuleProcessor.process(startTime, recallCancelRule, loan, recall);
                            return;
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.error("Unable to process recall event", e);
        }
    }
}
