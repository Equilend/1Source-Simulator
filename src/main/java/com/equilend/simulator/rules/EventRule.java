package com.equilend.simulator.rules;

import com.equilend.simulator.event.Event;

public class EventRule {

    private String eventType;
    private boolean shouldIgnore;

    public EventRule(String rule){
        loadRule(rule);
    }

    private void loadRule(String rule){
        int eventTypeStart = rule.indexOf("\"");
        int eventTypeEnd = rule.indexOf("\"", eventTypeStart+1);
        this.eventType = rule.substring(eventTypeStart+1, eventTypeEnd);
        int ignoreStart = rule.indexOf("\"", eventTypeEnd+1);
        if (rule.charAt(ignoreStart+1) == 'I'){
            shouldIgnore = true;
        }
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public boolean isShouldIgnore() {
        return shouldIgnore;
    }

    public void setShouldIgnore(boolean shouldIgnore) {
        this.shouldIgnore = shouldIgnore;
    }

    public boolean isApplicable(Event event){
        return event.getEventType().equals(eventType);
    }

    @Override
    public String toString(){
        if (shouldIgnore){
            return "Ignore " + eventType;
        }
        return "Dispatch " + eventType;
    }

}