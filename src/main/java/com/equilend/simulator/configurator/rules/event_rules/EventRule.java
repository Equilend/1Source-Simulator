package com.equilend.simulator.configurator.rules.event_rules;

import java.util.List;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.event.Event;

public class EventRule {

    private String eventType;
    private boolean ignore;

    public EventRule(String rule){
        loadRule(rule);
    }

    private void loadRule(String rule){
        List<String> args = RuleValidator.parseRule(rule);
        int idx = 0;
        this.eventType = args.get(idx++);
        this.ignore = args.get(idx++).equals("I");
    }

    public String getEventType() {
        return eventType;
    }

    public boolean shouldIgnore() {
        return ignore;
    }

    public boolean isApplicable(Event event){
        return eventType.equals("*") || eventType.equals(event.getEventType());
    }

    @Override
    public String toString(){
        if (ignore){
            return "Ignore " + eventType;
        }
        return "Dispatch " + eventType;
    }

}