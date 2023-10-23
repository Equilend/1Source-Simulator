package com.equilend.simulator.configurator.rules.agreement_rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.configurator.rules.RuleValidator;
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
        List<String> args = RuleValidator.parseRule(rule);
        int idx = 0;
        this.counterpartyExp = args.get(idx++);
        this.securityExp = args.get(idx++);
        this.quantityExp = args.get(idx++);
        ignore = args.get(idx++).equals("I");
        this.delay = Double.parseDouble(args.get(idx++));
    }

    private void splitExpressionAndLoad(String exp, Set<String> set){
        String[] arr = exp.split("\\|");
        for (String str : arr){
            set.add(str.trim());
        }
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
        return RuleValidator.validCounterparty(counterparties, cpty) && 
                RuleValidator.validSecurity(securities, trade.getInstrument())
                && RuleValidator.validQuantity(quantities, trade.getQuantity());
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