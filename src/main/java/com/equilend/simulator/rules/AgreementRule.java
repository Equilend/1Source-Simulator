package com.equilend.simulator.rules;

public class AgreementRule {
    private String counterParty;
    private String security;
    private String quantity;
    private boolean shouldIgnore;
    private int delay;

    public AgreementRule(String rule){
        loadRule(rule);
    }   

    private void loadRule(String rule){
        int start = rule.indexOf("\"");
        int end = rule.indexOf("\"", start+1);
        this.counterParty = rule.substring(start+1, end);
        start = rule.indexOf("\"", end+1);
        end = rule.indexOf("\"", start+1);
        this.security = rule.substring(start+1, end);
        start = rule.indexOf("\"", end+1);
        end = rule.indexOf("\"", start+1);
        this.quantity = rule.substring(start+1, end);
        start = rule.indexOf("\"", end+1);
        end = rule.indexOf("\"", start+1);
        if (rule.charAt(start+1) == 'I'){
            shouldIgnore = true;
        }
        start = rule.indexOf("\"", end+1);
        end = rule.indexOf("\"", start+1);
        this.delay = Integer.parseInt(rule.substring(start+1, end));
    }

    @Override 
    public String toString(){
        if (shouldIgnore){
            return "CPTY{" + counterParty + "}, SEC{" + security + "}, QTY{" + quantity + "}, IGNORE, DELAY{" + String.valueOf(delay) + "}";
        } else{
            return "CPTY{" + counterParty + "}, SEC{" + security + "}, QTY{" + quantity + "}, PROPOSE, DELAY{" + String.valueOf(delay) + "}";
        }
    }
}
