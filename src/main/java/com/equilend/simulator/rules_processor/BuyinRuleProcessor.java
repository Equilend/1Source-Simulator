package com.equilend.simulator.rules_processor;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.RuleException;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinAcceptRule;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinProposeRule;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinRule;
import com.equilend.simulator.service.BuyinService;
import com.os.client.model.BuyinComplete;
import com.os.client.model.Contract;

public class BuyinRuleProcessor {

    private static final Logger logger = LogManager.getLogger(BuyinRuleProcessor.class.getName());

    public static void process(Long startTime, BuyinRule rule, Contract contract, BuyinComplete buyin)
        throws APIException {

        if (rule instanceof BuyinAcceptRule) {
            acceptBuyin(startTime, (BuyinAcceptRule) rule, buyin);
        }

        if (rule instanceof BuyinProposeRule) {
            proposeBuying(startTime, (BuyinProposeRule) rule, contract);
        }
    }

    private static void acceptBuyin(Long startTime, BuyinAcceptRule rule, BuyinComplete buyin)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        BuyinService.acceptBuyin(buyin);
    }

    private static void proposeBuying(Long startTime, BuyinProposeRule rule, Contract contract)
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
        BuyinService.proposeBuyin(contract, quantity, priceValue);
    }

    private static void waitForDelay(Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
    }

}
