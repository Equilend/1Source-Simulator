package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.RuleException;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinAcceptRule;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinProposeRule;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinRule;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.service.BuyinService;
import java.util.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyinRuleProcessor {

    private static final Logger logger = LogManager.getLogger(BuyinRuleProcessor.class.getName());

    public static void process(Long startTime, BuyinRule rule, Loan loan, BuyinComplete buyin)
        throws APIException {

        if (rule instanceof BuyinAcceptRule) {
            processByAcceptRule(startTime, (BuyinAcceptRule) rule, buyin);
        }

        if (rule instanceof BuyinProposeRule) {
            processByProposeRule(startTime, (BuyinProposeRule) rule, loan);
        }
    }

    private static void processByAcceptRule(Long startTime, BuyinAcceptRule rule, BuyinComplete buyin)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        BuyinService.acceptBuyin(buyin);
    }

    private static void processByProposeRule(Long startTime, BuyinProposeRule rule, Loan loan)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        Integer quantity;
        try {
            quantity = Integer.parseInt(rule.getBuyinQuantity().stream().findFirst().get());
        } catch (NumberFormatException | NoSuchElementException e) {
            logger.error("Buyin Propose Rule must contain 'buyin_quantity' as number for new Buyin Propose");
            throw new RuleException(
                "Buyin Propose Rule must contain 'buyin_quantity' as number for new Buyin Propose");
        }

        Double priceValue = Double.parseDouble(rule.getPrice());
        BuyinService.proposeBuyin(loan, quantity, priceValue);
    }

}
