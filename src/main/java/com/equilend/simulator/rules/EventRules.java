package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.event.Event;

public class EventRules implements Rules{
    private List<EventRule> rules = new ArrayList<>();
    
    public EventRules(Map<String, Map<String, String>> rulesMap){
        // System.out.println("==-$-==-$-==-$-==-$-==-$-==-$-==");
        addRules(rulesMap.get("general").get("rules"));
        // System.out.println("==-$-==-$-==-$-==-$-==-$-==-$-==");
        addRules(rulesMap.get("general").get("tools"));
        // System.out.println("==-$-==-$-==-$-==-$-==-$-==-$-==");
        addRules(rulesMap.get("general").get("ghouls"));
        // System.out.println("==-$-==-$-==-$-==-$-==-$-==-$-==");
        addRules(rulesMap.get("general").get("mewls"));
        // System.out.println("==-$-==-$-==-$-==-$-==-$-==-$-==");
        addRules(rulesMap.get("general").get("jewels"));
        // System.out.println("==-$-==-$-==-$-==-$-==-$-==-$-==");                        

        for (EventRule rule : rules){
            System.out.println(rule);
        }

        //Use each rule line to instantiate an EventRule then add to rules list
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
            // System.out.println(rule);
            rules.add(new EventRule(rule));

            start = rulesList.indexOf("(", end);
        }

    }

    public boolean shouldIgnoreEvent(Event event){
        return false;
    }

}