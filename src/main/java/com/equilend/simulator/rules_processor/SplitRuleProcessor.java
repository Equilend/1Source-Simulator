package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.split_rules.SplitApproveRule;
import com.equilend.simulator.configurator.rules.split_rules.SplitProposeRule;
import com.equilend.simulator.configurator.rules.split_rules.SplitRule;
import com.equilend.simulator.service.SplitService;
import com.os.client.model.Loan;
import com.os.client.model.LoanSplit;

public class SplitRuleProcessor {

    private static final Logger logger = LogManager.getLogger(SplitRuleProcessor.class.getName());

    public static void process(Long startTime, SplitRule rule, Loan loan, LoanSplit loanSplit)
        throws APIException {

        if (rule instanceof SplitApproveRule) {
            logger.info("Processing Split Rule: Approve Contract Splits (SplitApproveRule). Loan: " + loan.getLoanId());
            processByApproveRule(startTime, (SplitApproveRule) rule, loan, loanSplit);
        }

        if (rule instanceof SplitProposeRule) {
            logger.info("Processing Split Rule: Propose Contract Splits (SplitProposeRule). Loan: " + loan.getLoanId());
            processByProposeRule(startTime, (SplitProposeRule) rule, loan);
        }

    }

    private static void processByApproveRule(Long startTime, SplitApproveRule rule, Loan loan,
        LoanSplit loanSplit)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        SplitService.approveSplit(loan, loanSplit);
    }

    private static void processByProposeRule(Long startTime, SplitProposeRule rule, Loan loan) throws APIException {
        waitForDelay(startTime, rule.getDelay());
        List<Integer> quantityList = rule.getSplitLotQuantity();
        Integer splitLotsSum = quantityList.stream().mapToInt(i -> i).sum();
        if (loan.getTrade().getOpenQuantity() > splitLotsSum) {
            quantityList = new ArrayList<>(quantityList);
            quantityList.add(loan.getTrade().getOpenQuantity() - splitLotsSum);
        }
        SplitService.proposeSplit(loan, quantityList);
    }
}
