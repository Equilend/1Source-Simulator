package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.event.Event;

public class EventRules implements Rules{
    private List<EventRule> rules = new ArrayList<>();
    
    public EventRules(Map<String, Map<String, String>> rulesMap){
        addRules(rulesMap.get("general").get("rules"));
    }

    public List<EventRule> getRules() {
        return rules;
    }

    public void addRules(String rulesList){
        if (rulesList.charAt(0) != '{') return;

        int start = rulesList.indexOf("(");
        while (start != -1){
            int end = rulesList.indexOf(")", start);
            
            String rule = rulesList.substring(start, end+1);
            rules.add(new EventRule(rule));
            
            start = rulesList.indexOf("(", end);
        }
    }

    public boolean shouldIgnoreEvent(Event event){
        for (EventRule rule : rules){
            if (rule.isApplicable(event)){
                return rule.isShouldIgnore();
            }
        }

        //Default to dispatching all events
        return false;
    }

}