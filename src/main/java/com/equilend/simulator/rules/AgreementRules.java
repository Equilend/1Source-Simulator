package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.trade.Trade;

public class AgreementRules implements Rules{

    private List<AgreementRule> rules = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger();

    public AgreementRules(Map<String, Map<String, String>> rulesMap){
        // logger.info("\n\nLoad Agreement Rules");
        addRules(rulesMap.get("general").get("responsive"));
    }

    public void addRules(String rulesList){
        if (rulesList == null) return;
        if (rulesList.charAt(0) != '{') return;

        int start = rulesList.indexOf(",(");
        while (start != -1){
            int end = rulesList.indexOf("),", start);
            
            String rule = rulesList.substring(start+1, end+1);
            AgreementRule agreementRule = new AgreementRule(rule);
            rules.add(agreementRule);
            // logger.info("String: {}", rule);
            // logger.info("Rule: {}",agreementRule);
            
            start = rulesList.indexOf(",(", end);
        }
    }
    
    // returns -1 if trade should be ignored, otherwise returns delay
    // defaults to propose immediately
    public double shouldIgnoreTrade(Trade trade, String partyId){
        for (AgreementRule rule : rules){
            if (rule.isApplicable(trade, partyId)){
                logger.trace("For Trade {}\n Apply Rule{}", trade.getInstrument().getTicker(), rule);
                return rule.isShouldIgnore() ? -1.0 : rule.getDelay();
            }
        }
        logger.trace("none applicable...");
        return 0;
    }
    
}