package com.equilend.simulator.configurator.rules.contract_rules;

import java.util.ArrayList;
import java.util.List;

import com.equilend.simulator.trade.transacting_party.PartyRole;

public class ContractGenerativeRule implements ContractRule {
    
    private String partyRoleExp;
    private PartyRole partyRole;
    private String counterpartyExp;
    private List<String> counterparties = new ArrayList<>();
    private String securityExp;
    private List<String> securities = new ArrayList<>();
    private String quantityExp;
    private Double delaySecs;
    private Double periodSecs;
    private Double totalDurationSecs;

    public ContractGenerativeRule(String rule){
        loadRule(rule);
        partyRole = (partyRoleExp.toUpperCase().equals("LENDER")) ? PartyRole.LENDER : PartyRole.BORROWER;
        splitExpressionAndLoad(counterpartyExp, counterparties);
        splitExpressionAndLoad(securityExp, securities);
    }

    private void loadRule(String rule){
        String delim = "\"";
        int start = rule.indexOf(delim);
        int end = rule.indexOf(delim, start+1);
        this.partyRoleExp = rule.substring(start+1, end);
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.counterpartyExp = rule.substring(start+1, end);
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);        
        this.securityExp = rule.substring(start+1, end);
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.quantityExp = rule.substring(start+1, end);
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.delaySecs = Double.parseDouble(rule.substring(start+1, end));
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.periodSecs = Double.parseDouble(rule.substring(start+1, end));
        start = rule.indexOf(delim, end+1);
        end = rule.indexOf(delim, start+1);
        this.totalDurationSecs = Double.parseDouble(rule.substring(start+1, end));
    }

    private void splitExpressionAndLoad(String exp, List<String> set){
        String[] arr = exp.split("\\|");
        for (String str : arr){
            set.add(str.trim());
        }
    }
    
    public PartyRole getPartyRole(){
        return partyRole;
    }

    public List<String> getCounterparties() {
        return counterparties;
    }

    public List<String> getSecurities() {
        return securities;
    }

    public String getQuantity() {
        return quantityExp;
    }

    public Double getDelaySecs() {
        return delaySecs;
    }

    public Double getPeriodSecs() {
        return periodSecs;
    }

    public Double getTotalDurationSecs() {
        return totalDurationSecs;
    }

    @Override
    public String toString(){
        return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp 
            + "}, DELAY{" + String.valueOf(delaySecs) + "}, Period{" + String.valueOf(periodSecs)
            + "}, DURATION{" + String.valueOf(totalDurationSecs) + "}";
    }
    
}
