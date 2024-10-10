package com.equilend.simulator.configurator.rules.return_rules;

import static com.equilend.simulator.configurator.rules.RulesParser.parseLogicalOr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.os.client.model.Loan;
import com.os.client.model.ModelReturn;
import com.os.client.model.TradeAgreement;
import com.os.client.model.TransactingParty;

public class ReturnProposeFromRecallRule implements ReturnRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> openQuantities = new HashSet<>();
    private final Set<String> recallQuantity = new HashSet<>();
    private String action;
    private Double delay;

    public ReturnProposeFromRecallRule(String rule) {
        loadRule(rule);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        counterparties.addAll(parseLogicalOr(args.get(0)));
        securities.addAll(parseLogicalOr(args.get(1)));
        openQuantities.addAll(parseLogicalOr(args.get(2)));
        recallQuantity.addAll(parseLogicalOr(args.get(3)));
        action = args.get(4);
        delay = Double.parseDouble(args.get(5));
    }

    public boolean isApplicable(ModelReturn oneSourceReturn, Loan loan, String partyId) {
        if (oneSourceReturn == null) {
            return false;
        }
        TradeAgreement trade = loan.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validQuantity(openQuantities, trade.getOpenQuantity());
    }

    public boolean isApplicable(Loan loan, String partyId) {
        TradeAgreement trade = loan.getTrade();
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

    public Set<String> getRecallQuantity(){
        return recallQuantity;
    }

}
