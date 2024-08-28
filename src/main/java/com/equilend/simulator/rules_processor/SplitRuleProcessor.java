package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.split_rules.SplitApproveRule;
import com.equilend.simulator.configurator.rules.split_rules.SplitProposeRule;
import com.equilend.simulator.configurator.rules.split_rules.SplitRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.split.ContractSplit;
import com.equilend.simulator.service.SplitService;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplitRuleProcessor {

    private static final Logger logger = LogManager.getLogger(SplitRuleProcessor.class.getName());

    public static void process(Long startTime, SplitRule rule, Contract contract, ContractSplit contractSplit)
        throws APIException {

        if (rule instanceof SplitApproveRule) {
            processByApproveRule(startTime, (SplitApproveRule) rule, contract, contractSplit);
        }

        if(rule instanceof SplitProposeRule) {
            processByProposeRule(startTime, (SplitProposeRule) rule, contract);
        }

    }

    private static void processByApproveRule(Long startTime, SplitApproveRule rule, Contract contract,
        ContractSplit contractSplit)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        SplitService.approveSplit(contract, contractSplit);
    }

    private static void processByProposeRule(Long startTime, SplitProposeRule rule, Contract contract)  throws APIException{
        waitForDelay(startTime, rule.getDelay());
        List<Integer> quantityList = rule.getSplitLotQuantity();
        SplitService.proposeSplit(contract, quantityList);
    }
}
