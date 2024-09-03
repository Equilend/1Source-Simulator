package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.recall_rules.RecallCancelRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallProposeRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallRule;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.recall.Recall;
import com.equilend.simulator.service.RecallService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecallRuleProcessor {

    private static final Logger logger = LogManager.getLogger(RecallRuleProcessor.class.getName());

    public static void process(Long startTime, RecallRule rule, Loan loan, Recall recall) throws APIException {

        if (rule instanceof RecallProposeRule) {
            processByProposeRule(startTime, (RecallProposeRule) rule, loan);
        }

        if (rule instanceof RecallCancelRule) {
            processByCancelRule(startTime, (RecallCancelRule) rule, loan, recall);
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

}
