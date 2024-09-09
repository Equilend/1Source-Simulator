package com.equilend.simulator.configurator.rules.event_rules;

import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.model.event.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventRules implements Rules {

    private final List<EventRule> rules = new ArrayList<>();

    public EventRules() {
    }

    public EventRules(Map<String, Map<String, String>> rulesMap) {
        if (rulesMap.containsKey("general")) {
            addRules(rulesMap.get("general").get("incoming"));
        }
    }

    public List<EventRule> getRules() {
        return rules;
    }

    public void addRules(String rulesList) {
        if (rulesList == null) {
            return;
        }
        if (rulesList.charAt(0) != '{') {
            return;
        }

        int start = rulesList.indexOf("(");
        while (start != -1) {
            int end = rulesList.indexOf(")", start);

            String rule = rulesList.substring(start, end + 1);
            EventRule eventRule = new EventRule(rule);
            rules.add(eventRule);

            start = rulesList.indexOf("(", end);
        }
    }

    public boolean shouldIgnoreEvent(Event event) {
        for (EventRule rule : rules) {
            if (rule.isApplicable(event)) {
                return rule.shouldIgnore();
            }
        }
        return true;
    }

}