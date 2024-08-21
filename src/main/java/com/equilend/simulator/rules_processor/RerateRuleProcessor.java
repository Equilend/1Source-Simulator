package com.equilend.simulator.rules_processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.ReratePendingCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateRule;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.service.RerateService;
import com.os.client.model.Contract;
import com.os.client.model.Rerate;

public class RerateRuleProcessor {

    private static final Logger logger = LogManager.getLogger(RerateRuleProcessor.class.getName());

    public static void process(Long startTime, RerateRule rule, Contract contract, Rerate rerate) throws APIException {

        if (rule instanceof RerateProposeRule) {
            postRerateProposal(startTime, (RerateProposeRule) rule, contract);
        }

        if (rule instanceof RerateCancelRule) {
            cancelRerateProposal(startTime, (RerateCancelRule) rule, contract, rerate);
        }

        if (rule instanceof RerateApproveRule) {
            RerateApproveRule rerateApproveRule = (RerateApproveRule) rule;
            if (rerateApproveRule.shouldApprove()) {
                approveRerateProposal(startTime, rerateApproveRule, contract, rerate);
            } else if (rerateApproveRule.shouldReject()) {
                declineRerateProposal(startTime, rerateApproveRule, contract, rerate);
            } else {
                //default to approve w/o delay
                approveRerateProposal(startTime, rerateApproveRule, contract, rerate);
            }
        }

        if (rule instanceof ReratePendingCancelRule) {
            cancelReratePending(startTime, (ReratePendingCancelRule) rule, contract, rerate);
        }
    }

    private static void postRerateProposal(Long startTime, RerateProposeRule rule, Contract contract)
        throws APIException {
        Double delay = rule.getDelay();
        Double delta = rule.getDelta();
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        RerateService.postRerateProposal(contract, delta);
    }

    private static void cancelRerateProposal(Long startTime, RerateCancelRule rule, Contract contract, Rerate rerate)
        throws APIException {
        long delayMillis = Math.round(1000 * rule.getDelay());
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        APIConnector.cancelRerateProposal(EventHandler.getToken(), contract.getContractId(), rerate.getRerateId());
    }

    private static void cancelReratePending(Long startTime, ReratePendingCancelRule rule, Contract contract,
        Rerate rerate)
        throws APIException {
        long delayMillis = Math.round(1000 * rule.getDelay());
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        RerateService.cancelRerateProposal(contract, rerate);
    }

    private static void approveRerateProposal(Long startTime, RerateApproveRule rule, Contract contract,
        Rerate rerate) throws APIException {
        long delayMillis = Math.round(1000 * rule.getDelay());
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        RerateService.approveRerateProposal(contract, rerate);
    }

    private static void declineRerateProposal(Long startTime, RerateApproveRule rule, Contract contract, Rerate rerate)
        throws APIException {
        long delayMillis = Math.round(1000 * rule.getDelay());
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        RerateService.declineRerateProposal(contract, rerate);
    }

}
