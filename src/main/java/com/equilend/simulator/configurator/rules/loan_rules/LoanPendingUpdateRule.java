package com.equilend.simulator.configurator.rules.loan_rules;

import static com.equilend.simulator.configurator.rules.RulesParser.parseLogicalOr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.os.client.model.Loan;
import com.os.client.model.TradeAgreement;
import com.os.client.model.TransactingParty;

public class LoanPendingUpdateRule implements LoanRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> quantities = new HashSet<>();
    private String action;
    private Double delay;

    public LoanPendingUpdateRule(String rule) {
        loadRule(rule);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        counterparties.addAll(parseLogicalOr(args.get(0)));
        securities.addAll(parseLogicalOr(args.get(1)));
        quantities.addAll(parseLogicalOr(args.get(2)));
        action = args.get(3);
        delay = Double.parseDouble(args.get(4));
    }

    public boolean isApplicable(Loan loan, String partyId) {
        if (loan == null) {
            return false;
        }
        TradeAgreement trade = loan.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validQuantity(quantities, trade.getQuantity());
    }

    private String getTradeCptyId(TradeAgreement trade, String partyId) {
        for (TransactingParty tp : trade.getTransactingParties()) {
            if (!tp.getParty().getPartyId().equals(partyId)) {
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean shouldUpdate() {
        return "US".equals(action);
    }

    public boolean isIgnored() {
        return "I".equals(action);
    }

    public Double getDelay() {
        return delay;
    }
}
