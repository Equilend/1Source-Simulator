package com.equilend.simulator.configurator.rules.return_rules;

import static com.equilend.simulator.configurator.rules.RulesParser.parseLogicalOr;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.returns.Return;
import com.equilend.simulator.model.trade.TradeAgreement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReturnProposeRule implements ReturnRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> openQuantities = new HashSet<>();
    private final Set<String> returnQuantity = new HashSet<>();
    private String action;
    private Double delay;

    public ReturnProposeRule(String rule) {
        loadRule(rule);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        counterparties.addAll(parseLogicalOr(args.get(0)));
        securities.addAll(parseLogicalOr(args.get(1)));
        openQuantities.addAll(parseLogicalOr(args.get(2)));
        returnQuantity.addAll(parseLogicalOr(args.get(3)));
        action = args.get(4);
        delay = Double.parseDouble(args.get(5));
    }

    public boolean isApplicable(Return oneSourceReturn, Contract contract, String partyId) {
        if (oneSourceReturn == null) {
            return false;
        }
        TradeAgreement trade = contract.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validQuantity(openQuantities, trade.getOpenQuantity())
            && RuleValidator.validQuantity(returnQuantity, oneSourceReturn.getQuantity());
    }

    public boolean isApplicable(Contract contract, String partyId) {
        TradeAgreement trade = contract.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validQuantity(openQuantities, trade.getOpenQuantity());
    }

    private String getTradeCptyId(TradeAgreement trade, String partyId) {
        for (TransactingParty tp : trade.getTransactingParties()) {
            if (!tp.getParty().getPartyId().equals(partyId)) {
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isIgnored() {
        return action.equals("I");
    }

    public boolean shouldPropose() {
        return action.equals("R");
    }

    public Double getDelay() {
        return delay;
    }

    public Set<String> getReturnQuantity(){
        return returnQuantity;
    }

}
