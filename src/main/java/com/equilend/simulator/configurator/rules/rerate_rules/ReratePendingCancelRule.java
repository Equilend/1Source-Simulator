package com.equilend.simulator.configurator.rules.rerate_rules;

import com.equilend.simulator.api.FedAPIException;
import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.trade.TradeAgreement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReratePendingCancelRule implements RerateRule {

    private String counterpartyExp;
    private final Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private final Set<String> securities = new HashSet<>();
    private String rateExp;
    private final Set<String> rates = new HashSet<>();
    private Boolean cancel = null;
    private Double delay;

    public ReratePendingCancelRule(String rule) {
        loadRule(rule);
        splitExpressionAndLoad(counterpartyExp, counterparties);
        splitExpressionAndLoad(securityExp, securities);
        splitExpressionAndLoad(rateExp, rates);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        int idx = 0;
        this.counterpartyExp = args.get(idx++);
        this.securityExp = args.get(idx++);
        this.rateExp = args.get(idx++);
        cancel = args.get(idx++).equals("CP");
        this.delay = Double.parseDouble(args.get(idx));
    }

    private void splitExpressionAndLoad(String exp, Set<String> set) {
        String[] arr = exp.split("\\|");
        for (String str : arr) {
            set.add(str.trim());
        }
    }

    public Double getDelay() {
        return delay;
    }

    public boolean shouldCancel() {
        return cancel;
    }

    private String getTradeCptyId(TradeAgreement trade, String partyId) {
        for (TransactingParty tp : trade.getTransactingParties()) {
            if (!tp.getParty().getPartyId().equals(partyId)) {
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isApplicable(Rerate rerate, Loan loan, String partyId) throws FedAPIException {
        if (rerate == null) {
            return false;
        }
        TradeAgreement trade = loan.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        boolean rebate = trade.getRate().getRebate() != null;
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validRate(rates, trade.getRate().getEffectiveRate(), trade.getInstrument().getSedol(),
            rebate);
    }

    @Override
    public String toString() {
        if (cancel != null) {
            if (cancel) {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp
                    + "}, CANCEL, DELAY{" + delay + "}";
            } else {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp
                    + "}, IGNORE, DELAY{" + delay + "}";
            }
        }
        return "";
    }

}