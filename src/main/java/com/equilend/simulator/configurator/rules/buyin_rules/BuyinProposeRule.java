package com.equilend.simulator.configurator.rules.buyin_rules;

import static com.equilend.simulator.configurator.rules.RulesParser.parseLogicalOr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.os.client.model.Loan;
import com.os.client.model.Recall;
import com.os.client.model.RecallStatus;
import com.os.client.model.TradeAgreement;
import com.os.client.model.TransactingParty;

public class BuyinProposeRule implements BuyinRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> buyinQuantity = new HashSet<>();
    private String price;
    private String action;
    private Double delay;

    public BuyinProposeRule(String rule) {
        loadRule(rule);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        counterparties.addAll(parseLogicalOr(args.get(0)));
        securities.addAll(parseLogicalOr(args.get(1)));
        buyinQuantity.addAll(parseLogicalOr(args.get(2)));
        price = args.get(3);
        action = args.get(4);
        delay = Double.parseDouble(args.get(5));
    }

    public boolean isApplicable(Recall recall, Loan loan, String partyId) {
        TradeAgreement trade = loan.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return RecallStatus.OPEN.equals(recall.getStatus())
            && RuleValidator.validCounterparty(counterparties, cpty)
            && RuleValidator.validSecurity(securities, trade.getInstrument());
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

    public boolean shouldSubmit() {
        return "S".equals(action);
    }

    public Double getDelay() {
        return delay;
    }

    public Set<String> getBuyinQuantity() {
        return buyinQuantity;
    }

    public String getPrice() {
        return price;
    }
}
