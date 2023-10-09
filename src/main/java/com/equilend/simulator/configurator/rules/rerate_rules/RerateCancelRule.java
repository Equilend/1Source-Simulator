package com.equilend.simulator.configurator.rules.rerate_rules;

import java.util.HashSet;
import java.util.Set;

import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.rerate.Rerate;
import com.equilend.simulator.trade.Trade;
import com.equilend.simulator.trade.transacting_party.TransactingParty;

public class RerateCancelRule implements RerateRule {
    
    private String counterpartyExp;
    private Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private Set<String> securities = new HashSet<>();
    private String quantityExp;
    private Set<String> quantities = new HashSet<>();
    private Boolean cancel = null;
    private Integer limit;
    private Double delay;

    public RerateCancelRule(String rule){
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
        if (rule.charAt(start+1) == 'C'){
            cancel = true;
        }
        else {
            cancel = false;
        }
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.limit = Integer.parseInt(rule.substring(start+1, end));
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

    private boolean validBasicQuantity(String basicQuantity, double rate){
        if (rate <= 0) return false;

        if (basicQuantity.equals("*")) return true;

        int delim = basicQuantity.indexOf(",");
        if (delim == -1) return false;

        String lowerStr = basicQuantity.substring(1, delim).trim();
        String upperStr = basicQuantity.substring(delim+1, basicQuantity.length()-1).trim();

        boolean lowerInclusive = basicQuantity.charAt(0) == '[';
        boolean upperInclusive = basicQuantity.charAt(basicQuantity.length()-1) == ']';
        
        long lower = Long.parseLong(lowerStr);
        long upper = (upperStr.equals("inf")) ? Long.MAX_VALUE : Long.parseLong(upperStr);
        
        return (lowerInclusive && rate >= lower || !lowerInclusive && rate > lower) 
        && (upperInclusive && rate <= upper || !upperInclusive && rate < upper);
        
    }
    
    private boolean validQuantity(double rate){
        for (String quantity : this.quantities){
            if (validBasicQuantity(quantity, rate)){
                return true;
            }
        }
        return false;
    }

    public Integer getLimit(){
        return limit;
    }

    public Double getDelay(){
        return delay;
    }

    public boolean shouldCancel(){
        return cancel;
    }

    private String getTradeCptyId(Trade trade, String partyId){
        for (TransactingParty tp : trade.getTransactingParties()){
            if (!tp.getParty().getPartyId().equals(partyId)){
                return tp.getParty().getPartyId();
            }
        }
        return "";        
    }

    public boolean isApplicable(Rerate rerate, Contract contract, String partyId){
        if (rerate == null) return false;
        Trade trade = contract.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return validCounterParty(cpty) && validSecurity(trade.getInstrument().getTicker())
                && validQuantity(rerate.getRerate().getEffectiveRate());
    }

    @Override
    public String toString(){
        if (cancel != null){
            if(cancel){
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp 
                        + "}, CANCEL, LIMIT{" + String.valueOf(limit) + "}, DELAY{" + String.valueOf(delay) + "}";
            } else{
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp 
                        + "}, IGNORE, LIMIT{" + String.valueOf(limit) + "}, DELAY{" + String.valueOf(delay) + "}";
            }            
        }
        return "";       
    }    

}