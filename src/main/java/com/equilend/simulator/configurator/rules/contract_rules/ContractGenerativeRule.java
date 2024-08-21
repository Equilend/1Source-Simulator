package com.equilend.simulator.configurator.rules.contract_rules;

import java.util.ArrayList;
import java.util.List;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.os.client.model.PartyRole;

public class ContractGenerativeRule implements ContractRule {

    private String partyRoleExp;
    private final PartyRole partyRole;
    private String counterpartyExp;
    private final List<String> counterparties = new ArrayList<>();
    private String securityExp;
    private final List<String> securities = new ArrayList<>();
    private String quantityExp;
    private Double delaySecs;
    private Double periodSecs;
    private Double totalDurationSecs;

    public ContractGenerativeRule(String rule) {
        loadRule(rule);
        partyRole = (partyRoleExp.equalsIgnoreCase("LENDER")) ? PartyRole.LENDER : PartyRole.BORROWER;
        splitExpressionAndLoad(counterpartyExp, counterparties);
        splitExpressionAndLoad(securityExp, securities);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        int idx = 0;
        this.partyRoleExp = args.get(idx++);
        this.counterpartyExp = args.get(idx++);
        this.securityExp = args.get(idx++);
        this.quantityExp = args.get(idx++);
        this.delaySecs = Double.parseDouble(args.get(idx++));
        this.periodSecs = Double.parseDouble(args.get(idx++));
        this.totalDurationSecs = Double.parseDouble(args.get(idx));
    }

    private void splitExpressionAndLoad(String exp, List<String> set) {
        String[] arr = exp.split("\\|");
        for (String str : arr) {
            set.add(str.trim());
        }
    }

    public PartyRole getPartyRole() {
        return partyRole;
    }

    public List<String> getCounterparties() {
        return counterparties;
    }

    public List<String> getSecurities() {
        return securities;
    }

    public String getQuantity() {
        return quantityExp;
    }

    public Double getDelaySecs() {
        return delaySecs;
    }

    public Double getPeriodSecs() {
        return periodSecs;
    }

    public Double getTotalDurationSecs() {
        return totalDurationSecs;
    }

    @Override
    public String toString() {
        return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp
            + "}, DELAY{" + delaySecs + "}, Period{" + periodSecs
            + "}, DURATION{" + totalDurationSecs + "}";
    }

}
