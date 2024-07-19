package com.equilend.simulator.configurator.rules.contract_rules;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.trade.TradeAgreement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContractResponsiveRule implements ContractRule {

    private String counterpartyExp;
    private final Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private final Set<String> securities = new HashSet<>();
    private String quantityExp;
    private final Set<String> quantities = new HashSet<>();
    private Boolean shouldIgnore = null;
    private Boolean shouldApprove = null;
    private Double delay;

    public ContractResponsiveRule(String rule) {
        loadRule(rule);
        splitExpressionAndLoad(counterpartyExp, counterparties);
        splitExpressionAndLoad(securityExp, securities);
        splitExpressionAndLoad(quantityExp, quantities);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        int idx = 0;
        this.counterpartyExp = args.get(idx++);
        this.securityExp = args.get(idx++);
        this.quantityExp = args.get(idx++);
        if (args.get(idx).equals("A")) {
            shouldApprove = true;
        }
        if (args.get(idx).equals("R")) {
            shouldApprove = false;
        }
        if (args.get(idx).equals("I")) {
            shouldIgnore = true;
        }
        if (args.get(idx++).equals("C")) {
            shouldIgnore = false;
        }
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

    public boolean isShouldApprove() {
        return shouldApprove;
    }

    public boolean isShouldIgnore() {
        return shouldIgnore;
    }

    private String getTradeCptyId(TradeAgreement trade, String partyId) {
        for (TransactingParty tp : trade.getTransactingParties()) {
            if (!tp.getParty().getPartyId().equals(partyId)) {
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isApplicable(Contract contract, String partyId) {
        if (contract == null) {
            return false;
        }
        TradeAgreement trade = contract.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validQuantity(quantities, trade.getQuantity());
    }

    @Override
    public String toString() {
        if (shouldIgnore != null) {
            if (shouldIgnore) {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp
                    + "}, IGNORE, DELAY{" + delay + "}";
            } else {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp
                    + "}, CANCEL, DELAY{" + delay + "}";
            }
        }
        if (shouldApprove != null) {
            if (shouldApprove) {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp
                    + "}, APPROVE, DELAY{" + delay + "}";
            } else {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp
                    + "}, REJECT, DELAY{" + delay + "}";
            }
        }
        return "";
    }

}