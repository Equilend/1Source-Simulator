package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.ReratePendingCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateRule;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.service.RerateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RerateRuleProcessor {

    private static final Logger logger = LogManager.getLogger(RerateRuleProcessor.class.getName());

    public static void process(Long startTime, RerateRule rule, Loan loan, Rerate rerate) throws APIException {

        if (rule instanceof RerateProposeRule) {
            postRerateProposal(startTime, (RerateProposeRule) rule, loan);
        }

        if (rule instanceof RerateCancelRule) {
            cancelRerateProposal(startTime, (RerateCancelRule) rule, loan, rerate);
        }

        if (rule instanceof RerateApproveRule) {
            RerateApproveRule rerateApproveRule = (RerateApproveRule) rule;
            if (rerateApproveRule.shouldApprove()) {
                approveRerateProposal(startTime, rerateApproveRule, loan, rerate);
            } else if (rerateApproveRule.shouldReject()) {
                declineRerateProposal(startTime, rerateApproveRule, loan, rerate);
            } else {
                //default to approve w/o delay
                approveRerateProposal(startTime, rerateApproveRule, loan, rerate);
            }
        }

        if (rule instanceof ReratePendingCancelRule) {
            cancelReratePending(startTime, (ReratePendingCancelRule) rule, loan, rerate);
        }
    }

    private static void postRerateProposal(Long startTime, RerateProposeRule rule, Loan loan)
        throws APIException {
        Double delta = rule.getDelta();
        waitForDelay(startTime, rule.getDelay());
        RerateService.postRerateProposal(loan, delta);
    }

    private static void cancelRerateProposal(Long startTime, RerateCancelRule rule, Loan loan, Rerate rerate)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        APIConnector.cancelRerateProposal(EventHandler.getToken(), loan.getLoanId(), rerate.getRerateId());
    }

    private static void cancelReratePending(Long startTime, ReratePendingCancelRule rule, Loan loan,
        Rerate rerate)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RerateService.cancelRerateProposal(loan, rerate);
    }

    private static void approveRerateProposal(Long startTime, RerateApproveRule rule, Loan loan,
        Rerate rerate) throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RerateService.approveRerateProposal(loan, rerate);
    }

    private static void declineRerateProposal(Long startTime, RerateApproveRule rule, Loan loan, Rerate rerate)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RerateService.declineRerateProposal(loan, rerate);
    }

}
