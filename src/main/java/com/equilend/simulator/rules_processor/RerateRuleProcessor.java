package com.equilend.simulator.rules_processor;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateRule;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.rate.FixedRateDef;
import com.equilend.simulator.model.rate.Rate;
import com.equilend.simulator.model.rate.RebateRate;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.rerate.RerateProposal;
import com.equilend.simulator.service.RerateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RerateRuleProcessor {

    private static final Logger logger = LogManager.getLogger(RerateRuleProcessor.class.getName());

    public static void process(Long startTime, RerateRule rule, Contract contract, Rerate rerate) {

        if (rule instanceof RerateProposeRule) {
            postRerateProposal(startTime, (RerateProposeRule) rule, contract);
        }
    }

    private static void postRerateProposal(Long startTime, RerateProposeRule rule, Contract contract) {
        Double delay = rule.getDelay();
        Double delta = rule.getDelta();
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }

        try {
            RerateService.postRerateProposal(contract, delta);
        } catch (APIException e) {
            logger.error("Unable to process rerate event", e);
        }
    }

}
