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
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.service.RerateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        Double delta = rule.getDelta();
        waitForDelay(startTime, rule.getDelay());
        RerateService.postRerateProposal(contract, delta);
    }

    private static void cancelRerateProposal(Long startTime, RerateCancelRule rule, Contract contract, Rerate rerate)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        APIConnector.cancelRerateProposal(EventHandler.getToken(), contract.getContractId(), rerate.getRerateId());
    }

    private static void cancelReratePending(Long startTime, ReratePendingCancelRule rule, Contract contract,
        Rerate rerate)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RerateService.cancelRerateProposal(contract, rerate);
    }

    private static void approveRerateProposal(Long startTime, RerateApproveRule rule, Contract contract,
        Rerate rerate) throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RerateService.approveRerateProposal(contract, rerate);
    }

    private static void declineRerateProposal(Long startTime, RerateApproveRule rule, Contract contract, Rerate rerate)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RerateService.declineRerateProposal(contract, rerate);
    }

}
