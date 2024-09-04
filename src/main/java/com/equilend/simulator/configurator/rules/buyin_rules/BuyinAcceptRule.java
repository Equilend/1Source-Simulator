package com.equilend.simulator.configurator.rules.buyin_rules;

import static com.equilend.simulator.configurator.rules.RulesParser.parseLogicalOr;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.trade.TradeAgreement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuyinAcceptRule implements BuyinRule {

    private final Set<String> counterparties = new HashSet<>();
    private final Set<String> securities = new HashSet<>();
    private final Set<String> buyinQuantity = new HashSet<>();
    private String price;
    private String action;
    private Double delay;

    public BuyinAcceptRule(String rule) {
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

    public boolean isApplicable(BuyinComplete buyin, Loan loan, String partyId) {
        if (buyin == null) {
            return false;
        }
        TradeAgreement trade = loan.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validQuantity(buyinQuantity, buyin.getQuantity())
            && RuleValidator.validDouble(price, buyin.getPrice().getValue());
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

    public boolean shouldAccept() {
        return "A".equals(action);
    }

    public Double getDelay() {
        return delay;
    }
}
