package com.equilend.simulator.configurator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.model.trade.instrument.Instrument;

public class RuleValidator {

    public static List<String> parseRule(String ruleStr){
        List<Integer> commaIdxs = new ArrayList<>();
        boolean withinRange = false;
        for (int i = 1; i < ruleStr.length()-1; i++){
            if (!withinRange && ruleStr.charAt(i) == ','){
                commaIdxs.add(i);
            }
            else if (ruleStr.charAt(i) == '(' || ruleStr.charAt(i) == '['){
                withinRange = true;
            }
            else if (ruleStr.charAt(i) == ')' || ruleStr.charAt(i) == ']'){
                withinRange = false;
            }
        }
        commaIdxs.add(ruleStr.length()-1);

        List<String> args = new ArrayList<>();
        int start = 1;
        for (int idx : commaIdxs){
            int colon = ruleStr.indexOf(":", start);
            if (colon >= 0 && start < idx) start = colon+1;
            String arg = ruleStr.substring(start, idx).trim();
            args.add(arg);
            start = idx+1; 
        }
 
        return args;
    }

    public static boolean validCounterparty(Set<String> counterparties, String counterparty){
        return counterparties.contains("*") || counterparties.contains(counterparty);
    }

    public static boolean validSecurity(Set<String> securities, Instrument security){
        if (securities.contains("*")) return true;

        for (String id : securities){
            int dash = id.indexOf("-");
            String idType = dash == -1 ? "T" : id.substring(0, dash).trim();
            String idValue = dash == -1 ? id : id.substring(dash+1);
            idValue = idValue.trim().toUpperCase();
            switch (idType.toUpperCase()){
                case "F":
                    if (idValue.equals(security.getFigi())) return true;
                    break;
                case "I":
                    if (idValue.equals(security.getIsin())) return true;
                    break;  
                case "S":
                    if (idValue.equals(security.getSedol())) return true;
                    break;  
                case "C":
                    if (idValue.equals(security.getCusip())) return true;
                    break;    
                default:
                    if (idValue.equals(security.getTicker())) return true;
                    break;
            }
        }
        return false;        
    }

    private static boolean validQuantityHelper(String quantityRange, long quantity){
        if (quantity <= 0) return false;

        if (quantityRange.equals("*")) return true;

        int delim = quantityRange.indexOf(",");
        if (delim == -1) return false;

        String lowerStr = quantityRange.substring(1, delim).trim();
        String upperStr = quantityRange.substring(delim+1, quantityRange.length()-1).trim().toUpperCase();

        boolean lowerInclusive = quantityRange.charAt(0) == '[';
        boolean upperInclusive = quantityRange.charAt(quantityRange.length()-1) == ']';
        
        long lower = Long.parseLong(lowerStr);
        long upper = (upperStr.equals("INF")) ? Long.MAX_VALUE : Long.parseLong(upperStr);
        
        return (lowerInclusive && quantity >= lower || !lowerInclusive && quantity > lower) 
            && (upperInclusive && quantity <= upper || !upperInclusive && quantity < upper);
    }
    
    public static boolean validQuantity(Set<String> quantities, long quantity){
        for (String quantityRange : quantities){
            if (validQuantityHelper(quantityRange, quantity)){
                return true;
            }
        }
        return false;
    }

    private static boolean validRateHelper(String rateRange, double rate){
        if (rate <= 0) return false;

        if (rateRange.equals("*")) return true;

        int delim = rateRange.indexOf(",");
        if (delim == -1) return false;

        String lowerStr = rateRange.substring(1, delim).trim().toUpperCase();
        String upperStr = rateRange.substring(delim+1, rateRange.length()-1).trim().toUpperCase();

        boolean lowerInclusive = rateRange.charAt(0) == '[';
        boolean upperInclusive = rateRange.charAt(rateRange.length()-1) == ']';
        
        Double lower = (lowerStr.equals("-INF")) ? Double.MIN_VALUE : Double.parseDouble(lowerStr);
        Double upper = (upperStr.equals("INF")) ? Double.MAX_VALUE : Double.parseDouble(upperStr);
        
        return (lowerInclusive && rate >= lower || !lowerInclusive && rate > lower) 
        && (upperInclusive && rate <= upper || !upperInclusive && rate < upper);
        
    }
    
    public static boolean validRate(Set<String> rates, double rate){
        for (String rateRange : rates){
            if (validRateHelper(rateRange, rate)){
                return true;
            }
        }
        return false;
    }    

}
