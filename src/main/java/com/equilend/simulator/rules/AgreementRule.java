package com.equilend.simulator.rules;

import com.equilend.simulator.trade.Trade;
import com.equilend.simulator.trade.transacting_party.TransactingParty;

public class AgreementRule {
    private String counterParty;
    private String security;
    private String quantity;
    private boolean shouldIgnore;
    private int delay;
    private String partyId;

    public AgreementRule(String rule, String partyId){
        loadRule(rule);
        this.partyId = partyId;
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

    public boolean validCounterParty(String cpty){
        return counterParty.equals("*") || counterParty.equals(cpty);
    }

    public boolean validSecurity(String scty){
        return security.equals("*") || security.equals(scty);
    }

    public boolean validQuantity(long tradeQty){
        if (quantity.equals("*")) return true;

        int digitStart = 0;
        while (!Character.isDigit(quantity.charAt(digitStart))){
            digitStart++;
        }
        String comp = quantity.substring(0, digitStart).trim();
        long bound = Long.parseLong(quantity.substring(digitStart).trim());
        switch (comp){
            case "<":
                return tradeQty < bound;
            case "<=":
                return tradeQty <= bound;
            case ">":
                return tradeQty > bound;
            case ">=":
                return tradeQty >= bound;
            case "==":
                return tradeQty == bound;
        }
        
        return false;
    }

    public boolean isShouldIgnore() {
        return shouldIgnore;
    }
    
    public String getTradeCptyId(Trade trade){
        for (TransactingParty tp : trade.getTransactingParties()){
            if (!tp.getParty().getPartyId().equals(partyId)){
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isApplicable(Trade trade){
        String cpty = getTradeCptyId(trade);
        return validCounterParty(cpty) && validSecurity(trade.getInstrument().getTicker())
                && validQuantity(trade.getQuantity());
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
