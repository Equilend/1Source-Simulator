package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.recall_rules.RecallAcknowledgeRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallCancelRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallProposeRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallRule;
import com.equilend.simulator.service.RecallService;
import com.os.client.model.AcknowledgementType;
import com.os.client.model.Loan;
import com.os.client.model.Recall;

public class RecallRuleProcessor {

    private static final Logger logger = LogManager.getLogger(RecallRuleProcessor.class.getName());

    public static void process(Long startTime, RecallRule rule, Loan loan, Recall recall) throws APIException {

        if (rule instanceof RecallProposeRule) {
            logger.debug("Processing Recall Rule: Propose Recalls (RecallProposeRule). Loan:  " + loan.getLoanId());
            processByProposeRule(startTime, (RecallProposeRule) rule, loan);
        }

        if (rule instanceof RecallCancelRule) {
            logger.debug("Processing Recall Rule: Cancel Recall (RecallCancelRule). Loan: " + loan.getLoanId());
            processByCancelRule(startTime, (RecallCancelRule) rule, loan, recall);
        }

        if(rule instanceof RecallAcknowledgeRule) {
            logger.debug("Processing Recall Rule: Acknowledge Recall (RecallAcknowledgeRule). Loan: " + loan.getLoanId());
            processByAcknowledgeRule(startTime, (RecallAcknowledgeRule) rule, loan, recall);
        }

    }

    private static void processByProposeRule(Long startTime, RecallProposeRule rule, Loan loan)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RecallService.proposeRecall(loan, rule.getRecallQuantity());
    }

    private static void processByCancelRule(Long startTime, RecallCancelRule rule, Loan loan, Recall recall)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RecallService.cancelRecall(loan.getLoanId(), recall.getRecallId());
    }

    private static void processByAcknowledgeRule(Long startTime, RecallAcknowledgeRule rule, Loan loan, Recall recall)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        if(rule.shouldAcknowledgePositively()){
            RecallService.postRecallAck(loan, recall, AcknowledgementType.POSITIVE);
        }

        if(rule.shouldAcknowledgeNegatively()){
            RecallService.postRecallAck(loan, recall, AcknowledgementType.NEGATIVE);
        }
    }

}
