package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.recall_rules.RecallCancelRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallProposeRule;
import com.equilend.simulator.configurator.rules.recall_rules.RecallRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.recall.Recall;
import com.equilend.simulator.service.RecallService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecallRuleProcessor {

    private static final Logger logger = LogManager.getLogger(RecallRuleProcessor.class.getName());

    public static void process(Long startTime, RecallRule rule, Contract contract, Recall recall) throws APIException {

        if (rule instanceof RecallProposeRule) {
            processByProposeRule(startTime, (RecallProposeRule) rule, contract);
        }

        if (rule instanceof RecallCancelRule) {
            processByCancelRule(startTime, (RecallCancelRule) rule, contract, recall);
        }

    }

    private static void processByProposeRule(Long startTime, RecallProposeRule rule, Contract contract)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RecallService.proposeRecall(contract, rule.getRecallQuantity());
    }

    private static void processByCancelRule(Long startTime, RecallCancelRule rule, Contract contract, Recall recall)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        RecallService.cancelRecall(contract.getContractId(), recall.getRecallId());
    }

}
