package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.LoanService.getLoanById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.loan_rules.LoanApproveRejectRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanCancelRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanPendingCancelRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanPendingUpdateRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallProposeRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeFromLoanRule;
import com.equilend.simulator.configurator.rules.split_rules.SplitProposeRule;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.rules_processor.LoanRuleProcessor;
import com.equilend.simulator.rules_processor.RecallRuleProcessor;
import com.equilend.simulator.rules_processor.RerateRuleProcessor;
import com.equilend.simulator.rules_processor.ReturnRuleProcessor;
import com.equilend.simulator.rules_processor.SplitRuleProcessor;
import com.equilend.simulator.service.LoanService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoanHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(LoanHandler.class.getName());
    private final Event event;
    private final Config config;
    private final String botPartyId;
    private final Long startTime;

    public LoanHandler(Event e, Config config, Long startTime) {
        this.event = e;
        this.config = config;
        this.botPartyId = config.getBotPartyId();
        this.startTime = startTime;
    }


    public void run() {
        //Parse loan id
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String loanId = arr[arr.length - 1];

        try {
            Loan loan = getLoanById(loanId);
            if (loan == null) {
                return;
            }

            boolean isInitiator = LoanService.isInitiator(loan, botPartyId);
            logger.debug("Is initiator?: " + isInitiator);
            switch (event.getEventType()) {
                case LOAN_PROPOSED:
                    if (isInitiator) {
                        LoanCancelRule loanCancelRule = config.getLoanRules()
                            .getLoanCancelRule(loan, botPartyId);
                        if (loanCancelRule != null && loanCancelRule.shouldCancel()) {
                            LoanRuleProcessor.process(startTime, loanCancelRule, loan);
                            return;
                        }
                    } else {
                        LoanApproveRejectRule loanApproveRejectRule = config.getLoanRules()
                            .getLoanApproveRejectRule(loan, botPartyId);
                        if (loanApproveRejectRule != null && !loanApproveRejectRule.shouldIgnore()) {
                            LoanRuleProcessor.process(startTime, loanApproveRejectRule, loan, botPartyId);
                            return;
                        }
                    }
                    logger.debug("Event {} with ResourceUri {} has not been processed by rules", event.getEventType(), event.getResourceUri());
                    break;
                case LOAN_OPENED:
                    RerateProposeRule rerateProposeRule = config.getRerateRules()
                        .getProposeRule(loan, botPartyId);
                    if (rerateProposeRule != null && rerateProposeRule.shouldPropose()) {
                        RerateRuleProcessor.process(startTime, rerateProposeRule, loan, null);
                        return;
                    }

                    ReturnProposeFromLoanRule returnProposeRule = config.getReturnRules()
                        .getReturnProposeFromLoanRule(loan, botPartyId);
                    if (returnProposeRule != null && returnProposeRule.shouldPropose()) {
                        ReturnRuleProcessor.process(startTime, returnProposeRule, loan, null);
                        return;
                    }

                    RecallProposeRule recallProposeRule = config.getRecallRules()
                        .getRecallProposeRule(loan, botPartyId);
                    if (recallProposeRule != null && recallProposeRule.shouldPropose()) {
                        RecallRuleProcessor.process(startTime, recallProposeRule, loan, null);
                        return;
                    }

                    SplitProposeRule splitProposeRule = config.getSplitRules()
                        .getSplitProposeRule(loan, botPartyId);
                    if (splitProposeRule != null && splitProposeRule.shouldPropose()) {
                        SplitRuleProcessor.process(startTime, splitProposeRule, loan, null);
                        return;
                    }
                    logger.debug("Event {} with ResourceUri {} has not been processed by rules", event.getEventType(), event.getResourceUri());
                    break;
                case LOAN_PENDING:
                    LoanPendingCancelRule loanPendingCancelRule = config.getLoanRules()
                        .getLoanPendingCancelRule(loan, botPartyId);
                    if (loanPendingCancelRule != null && loanPendingCancelRule.shouldCancel()) {
                        LoanRuleProcessor.process(startTime, loanPendingCancelRule, loan);
                        return;
                    }

                    LoanPendingUpdateRule loanPendingUpdateRule = config.getLoanRules()
                        .getLoanPendingUpdateRule(loan, botPartyId);
                    if (loanPendingUpdateRule != null && loanPendingUpdateRule.shouldUpdate()) {
                        LoanRuleProcessor.process(startTime, loanPendingUpdateRule, loan);
                        return;
                    }
                    logger.debug("Event {} with ResourceUri {} has not been processed by rules", event.getEventType(), event.getResourceUri());
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.debug("Unable to process loan event", e);
        }

    }

}