package com.equilend.simulator.configurator.rules.recall_rules;

import static com.equilend.simulator.configurator.rules.RulesParser.parseLogicalOr;

import com.equilend.simulator.configurator.rules.RuleException;
import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.trade.TradeAgreement;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class RecallProposeRule implements RecallRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> openQuantities = new HashSet<>();
    private final Set<String> recallQuantity = new HashSet<>();
    private String action;
    private Double delay;

    public RecallProposeRule(String rule) {
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
        return "I".equals(action);
    }

    public boolean shouldPropose() {
        return "R".equals(action);
    }

    public Double getDelay() {
        return delay;
    }

    public Integer getRecallQuantity(){
        try {
            return Integer.parseInt(recallQuantity.stream().findFirst().get());
        } catch (NumberFormatException | NoSuchElementException e) {
            throw new RuleException(
                "Recall Propose Rule must contain 'recall_quantity' as number for new Recall Propose");
        }
    }

}
