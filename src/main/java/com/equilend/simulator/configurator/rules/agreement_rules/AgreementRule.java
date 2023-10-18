package com.equilend.simulator.configurator.rules.agreement_rules;

import java.util.HashSet;
import java.util.Set;

import com.equilend.simulator.model.trade.Trade;
import com.equilend.simulator.model.trade.transacting_party.TransactingParty;

public class AgreementRule {

    private String counterpartyExp;
    private Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private Set<String> securities = new HashSet<>();
    private String quantityExp;
    private Set<String> quantities = new HashSet<>();
    private boolean ignore;
    private Double delay;

    public AgreementRule(String rule){
        loadRule(rule);
        splitExpressionAndLoad(counterpartyExp, counterparties);
        splitExpressionAndLoad(securityExp, securities);
        splitExpressionAndLoad(quantityExp, quantities);
    }   

    private void loadRule(String rule){
        String delim = "\"";
        int start = rule.indexOf(delim);
        int end = rule.indexOf(delim, start+1);
        this.counterpartyExp = rule.substring(start+1, end);        
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.securityExp = rule.substring(start+1, end);
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.quantityExp = rule.substring(start+1, end);
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        if (rule.charAt(start+1) == 'I'){
            ignore = true;
        }
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.delay = Double.parseDouble(rule.substring(start+1, end));
    }

    private void splitExpressionAndLoad(String exp, Set<String> set){
        String[] arr = exp.split("\\|");
        for (String str : arr){
            set.add(str.trim());
        }
    }

    private boolean validCounterParty(String cpty){
        return counterpartyExp.equals("*") || counterparties.contains(cpty);
    }

    private boolean validSecurity(String scty){
        return securityExp.equals("*") || securities.contains(scty);
    }

    private boolean validBasicQuantity(String basicQuantity, long tradeQty){
        if (tradeQty <= 0) return false;

        if (basicQuantity.equals("*")) return true;

        int delim = basicQuantity.indexOf(",");
        if (delim == -1) return false;

        String lowerStr = basicQuantity.substring(1, delim).trim();
        String upperStr = basicQuantity.substring(delim+1, basicQuantity.length()-1).trim();

        boolean lowerInclusive = basicQuantity.charAt(0) == '[';
        boolean upperInclusive = basicQuantity.charAt(basicQuantity.length()-1) == ']';
        
        long lower = Long.parseLong(lowerStr);
        long upper = (upperStr.toUpperCase().equals("INF")) ? Long.MAX_VALUE : Long.parseLong(upperStr);
        
        return (lowerInclusive && tradeQty >= lower || !lowerInclusive && tradeQty > lower) 
            && (upperInclusive && tradeQty <= upper || !upperInclusive && tradeQty < upper);
    }
    
    private boolean validQuantity(long tradeQty){
        for (String quantity : this.quantities){
            if (validBasicQuantity(quantity, tradeQty)){
                return true;
            }
        }
        return false;
    }

    public Double getDelay(){
        return delay;
    }

    public boolean shouldIgnore() {
        return ignore;
    }
    
    public String getTradeCptyId(Trade trade, String partyId){
        for (TransactingParty tp : trade.getTransactingParties()){
            if (!tp.getParty().getPartyId().equals(partyId)){
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isApplicable(Trade trade, String partyId){
        String cpty = getTradeCptyId(trade, partyId);
        return validCounterParty(cpty) && validSecurity(trade.getInstrument().getTicker())
                && validQuantity(trade.getQuantity());
    }

    @Override 
    public String toString(){
        if (ignore){
            return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp + "}, IGNORE, DELAY{" + String.valueOf(delay) + "}";
        } else{
            return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp + "}, PROPOSE, DELAY{" + String.valueOf(delay) + "}";
        }
    }

}