package com.equilend.simulator.rules;

import java.util.HashSet;
import java.util.Set;

import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.trade.Trade;
import com.equilend.simulator.trade.transacting_party.TransactingParty;

public class ContractRule {
    
    private String counterpartyExp;
    private Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private Set<String> securities = new HashSet<>();
    private String quantityExp;
    private Set<String> quantities = new HashSet<>();
    private boolean shouldReject;
    private boolean shouldIgnore;
    private int timeout;
    private String partyId;

    public ContractRule(String rule, String partyId){
        loadRule(rule);
        this.partyId = partyId;

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
        if (rule.charAt(start+1) == 'R'){
            shouldReject = true;
        }
        if (rule.charAt(start+1) == 'I'){
            shouldIgnore = true;
        }        
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.timeout = Integer.parseInt(rule.substring(start+1, end));
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
        long upper = (upperStr.equals("inf")) ? Long.MAX_VALUE : Long.parseLong(upperStr);
        
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

    public boolean isShouldReject(){
        return shouldReject;
    }

    public boolean isShouldIgnore() {
        return shouldIgnore;
    }
    
    public String getTradeCptyId(Trade trade){
        for (TransactingParty tp : trade.getTransactingParties()){
            if (!tp.getParty().getPartyId().equals(this.partyId)){
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isApplicable(Contract contract){
        if (contract == null) return false;
        Trade trade = contract.getTrade();
        String cpty = getTradeCptyId(trade);
        return validCounterParty(cpty) && validSecurity(trade.getInstrument().getTicker())
                && validQuantity(trade.getQuantity());
    }

    @Override 
    public String toString(){
        if (shouldIgnore){
            return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp + "}, IGNORE, DELAY{" + String.valueOf(timeout) + "}";
        } else{
            return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp + "}, PROPOSE, DELAY{" + String.valueOf(timeout) + "}";
        }
    }

}