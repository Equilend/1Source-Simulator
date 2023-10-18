package com.equilend.simulator.configurator.rules.event_rules;

import com.equilend.simulator.model.event.Event;

public class EventRule {

    private String eventType;
    private boolean shouldIgnore;

    public EventRule(String rule){
        loadRule(rule);
    }

    private void loadRule(String rule){
        int eventTypeStart = rule.indexOf("\"");
        int eventTypeEnd = rule.indexOf("\"", eventTypeStart+1);
        this.eventType = rule.substring(eventTypeStart+1, eventTypeEnd).toUpperCase();
        int ignoreStart = rule.indexOf("\"", eventTypeEnd+1);
        if (rule.charAt(ignoreStart+1) == 'I'){
            shouldIgnore = true;
        }
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isShouldIgnore() {
        return shouldIgnore;
    }

    public boolean isApplicable(Event event){
        return eventType.equals("*") || eventType.equals(event.getEventType());
    }

    @Override
    public String toString(){
        if (shouldIgnore){
            return "Ignore " + eventType;
        }
        return "Dispatch " + eventType;
    }

}