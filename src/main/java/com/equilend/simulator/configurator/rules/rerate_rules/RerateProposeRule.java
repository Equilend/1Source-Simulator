package com.equilend.simulator.configurator.rules.rerate_rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.api.FedAPIException;
import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.trade.Trade;
import com.equilend.simulator.model.trade.transacting_party.TransactingParty;

public class RerateProposeRule implements RerateRule {
    
    private String counterpartyExp;
    private Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private Set<String> securities = new HashSet<>();
    private String rateExp;
    private Set<String> rates = new HashSet<>();
    private Boolean propose = null;
    private Double delta;
    private Double delay;

    public RerateProposeRule(String rule){
        loadRule(rule);
        splitExpressionAndLoad(counterpartyExp, counterparties);
        splitExpressionAndLoad(securityExp, securities);
        splitExpressionAndLoad(rateExp, rates);
    }

    private void loadRule(String rule){
        List<String> args = RuleValidator.parseRule(rule);
        int idx = 0;
        this.counterpartyExp = args.get(idx++);
        this.securityExp = args.get(idx++);
        this.rateExp = args.get(idx++);
        this.delta = Double.parseDouble(args.get(idx++));
        propose = args.get(idx++).equals("P");
        this.delay = Double.parseDouble(args.get(idx++));
    }    

    private void splitExpressionAndLoad(String exp, Set<String> set){
        String[] arr = exp.split("\\|");
        for (String str : arr){
            set.add(str.trim());
        }
    }


    public Double getDelta(){
        return delta;
    }

    public Double getDelay(){
        return delay;
    }

    public boolean shouldPropose(){
        return propose;
    }

    private String getTradeCptyId(Trade trade, String partyId){
        for (TransactingParty tp : trade.getTransactingParties()){
            if (!tp.getParty().getPartyId().equals(partyId)){
                return tp.getParty().getPartyId();
            }
        }
        return "";        
    }

    public boolean isApplicable(Contract contract, String partyId) throws FedAPIException{
        Trade trade = contract.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        boolean rebate = trade.getRate().getRebate() != null;
        return RuleValidator.validCounterparty(counterparties, cpty) && 
                RuleValidator.validSecurity(securities, trade.getInstrument())
                && RuleValidator.validRate(rates, trade.getRate().getEffectiveRate(), trade.getInstrument().getSedol(), rebate);
    }

    @Override
    public String toString(){
        if (propose != null){
            if(propose){
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp + "}, PROPOSE, DELTA{"
                        + String.valueOf(delta) + "}, DELAY{" + String.valueOf(delay) + "}";
            } else{
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp + "}, IGNORE, DELTA{"
                        + String.valueOf(delta) + "}, DELAY{" + String.valueOf(delay) + "}";
            }            
        }
        return "";       
    }    

}