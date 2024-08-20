package com.equilend.simulator.rules_processor;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinAcceptRule;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinRule;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.service.BuyinService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyinRuleProcessor {

    private static final Logger logger = LogManager.getLogger(BuyinRuleProcessor.class.getName());

    public static void process(Long startTime, BuyinRule rule, Contract contract, BuyinComplete buyin)
        throws APIException {

        if (rule instanceof BuyinAcceptRule) {
            acceptBuyin(startTime, (BuyinAcceptRule) rule, contract, buyin);
        }
    }

    private static void acceptBuyin(Long startTime, BuyinAcceptRule rule, Contract contract, BuyinComplete buyin)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        BuyinService.acceptBuyin(buyin);
    }

    private static void waitForDelay(Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
    }

}
