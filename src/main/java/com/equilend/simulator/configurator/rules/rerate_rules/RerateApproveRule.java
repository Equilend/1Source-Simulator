package com.equilend.simulator.configurator.rules.rerate_rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.api.FedAPIException;
import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.trade.Trade;
import com.equilend.simulator.model.trade.transacting_party.TransactingParty;

public class RerateApproveRule implements RerateRule {
    
    private String counterpartyExp;
    private final Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private final Set<String> securities = new HashSet<>();
    private String rateExp;
    private final Set<String> rates = new HashSet<>();
    private Boolean approve = null;
    private Double delay;

    public RerateApproveRule(String rule){
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
        approve = args.get(idx++).equals("A");
        this.delay = Double.parseDouble(args.get(idx));
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

    public boolean shouldApprove(){
        return approve;
    }

    private String getTradeCptyId(Trade trade, String partyId){
        for (TransactingParty tp : trade.getTransactingParties()){
            if (!tp.getParty().getPartyId().equals(partyId)){
                return tp.getParty().getPartyId();
            }
        }
        return "";        
    }



    public boolean isApplicable(Rerate rerate, Contract contract, String partyId) throws FedAPIException{
        if (rerate == null) return false;
        Trade trade = contract.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        boolean rebate = trade.getRate().getRebate() != null;
        return RuleValidator.validCounterparty(counterparties, cpty) && 
                RuleValidator.validSecurity(securities, trade.getInstrument())
                && RuleValidator.validRate(rates, trade.getRate().getEffectiveRate(), trade.getInstrument().getSedol(), rebate);
    }

    @Override
    public String toString(){
        if (approve != null){
            if(approve){
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp 
                        + "}, APPROVE, DELAY{" + delay + "}";
            } else{
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp 
                        + "}, REJECT, DELAY{" + delay + "}";
            }            
        }
        return "";       
    }    

}