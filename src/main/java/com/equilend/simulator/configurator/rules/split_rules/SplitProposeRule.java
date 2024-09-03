package com.equilend.simulator.configurator.rules.split_rules;

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
import java.util.stream.Collectors;

public class SplitProposeRule implements SplitRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> openQuantities = new HashSet<>();
    private final Set<String> splitLot = new HashSet<>();
    private String action;
    private Double delay;

    public SplitProposeRule(String rule) {
        loadRule(rule);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        counterparties.addAll(parseLogicalOr(args.get(0)));
        securities.addAll(parseLogicalOr(args.get(1)));
        openQuantities.addAll(parseLogicalOr(args.get(2)));
        splitLot.addAll(parseLogicalOr(args.get(3)));
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

    public boolean shouldPropose() {
        return "P".equals(action);
    }

    public boolean isIgnored() {
        return "I".equals(action);
    }

    public Double getDelay() {
        return delay;
    }

    public List<Integer> getSplitLotQuantity(){
        try {
            return splitLot.stream().map(splitLotStr -> Integer.parseInt(splitLotStr)).collect(Collectors.toList());
        } catch (NumberFormatException | NoSuchElementException e) {
            throw new RuleException(
                "Split Propose Rule must contain 'split_lot' as number for new Loan Split");
        }
    }
}
