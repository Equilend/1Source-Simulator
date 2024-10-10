package com.equilend.simulator.configurator.rules.split_rules;

import static com.equilend.simulator.configurator.rules.RulesParser.parseLogicalOr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.os.client.model.Loan;
import com.os.client.model.LoanSplit;
import com.os.client.model.TradeAgreement;
import com.os.client.model.TransactingParty;

public class SplitApproveRule implements SplitRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> openQuantities = new HashSet<>();
    private String action;
    private Double delay;

    public SplitApproveRule(String rule) {
        loadRule(rule);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        counterparties.addAll(parseLogicalOr(args.get(0)));
        securities.addAll(parseLogicalOr(args.get(1)));
        openQuantities.addAll(parseLogicalOr(args.get(2)));
        action = args.get(3);
        delay = Double.parseDouble(args.get(4));
    }

    public boolean isApplicable(LoanSplit loanSplit, Loan loan, String partyId) {
        if (loanSplit == null) {
            return false;
        }
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

    public boolean shouldApprove() {
        return "A".equals(action);
    }

    public boolean isIgnored() {
        return "I".equals(action);
    }

    public Double getDelay() {
        return delay;
    }
}
