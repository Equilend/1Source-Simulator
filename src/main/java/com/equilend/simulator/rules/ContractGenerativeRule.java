package com.equilend.simulator.rules;

import java.util.ArrayList;
import java.util.List;

public class ContractGenerativeRule implements ContractRule {
    
    private String counterpartyExp;
    private List<String> counterparties = new ArrayList<>();
    private String securityExp;
    private List<String> securities = new ArrayList<>();
    private String quantityExp;
    private List<String> quantities = new ArrayList<>();
    private int delay;
    private double frequency;
    private int maxGenerated;

    public ContractGenerativeRule(String rule){
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
        this.delay = Integer.parseInt(rule.substring(start+1, end));
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.frequency = Double.parseDouble(rule.substring(start+1, end));
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.maxGenerated = Integer.parseInt(rule.substring(start+1, end));
    }

    private void splitExpressionAndLoad(String exp, List<String> set){
        String[] arr = exp.split("\\|");
        for (String str : arr){
            set.add(str.trim());
        }
    }   

    public List<String> getCounterparties() {
        return counterparties;
    }

    public List<String> getSecurities() {
        return securities;
    }

    public List<String> getQuantities() {
        return quantities;
    }

    public int getDelay() {
        return delay;
    }

    public double getFrequency() {
        return frequency;
    }

    public int getMaxGenerated() {
        return maxGenerated;
    }

    @Override
    public String toString(){
        return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp 
            + "}, DELAY{" + String.valueOf(delay) + "}, FREQUENCY{" + String.valueOf(frequency)
            + "}, MAX_GENERATED{" + String.valueOf(maxGenerated) + "}";
    }
    
}
