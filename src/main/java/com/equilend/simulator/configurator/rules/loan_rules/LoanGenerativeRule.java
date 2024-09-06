package com.equilend.simulator.configurator.rules.loan_rules;

import com.equilend.simulator.configurator.rules.RuleValidator;
import java.util.ArrayList;
import java.util.List;

public class LoanGenerativeRule implements LoanRule {

    private String partyRole;
    private String counterpartyExp;
    private final List<String> counterparties = new ArrayList<>();
    private String securityExp;
    private final List<String> securities = new ArrayList<>();
    private String quantityExp;
    private Integer quantity;
    private String rateExp;
    private Double rate;
    private String priceExp;
    private Double price;
    private String termType;
    private Double delaySecs;
    private Double periodSecs;
    private Double totalDurationSecs;

    public LoanGenerativeRule(String rule) {
        loadRule(rule);
        splitExpressionAndLoad(counterpartyExp, counterparties);
        splitExpressionAndLoad(securityExp, securities);
    }

    private void loadRule(String rule) {
        List<String> args = RuleValidator.parseRule(rule);
        int idx = 0;
        this.partyRole = args.get(idx++);
        this.counterpartyExp = args.get(idx++);
        this.securityExp = args.get(idx++);
        this.quantityExp = args.get(idx++);
        this.rateExp = args.get(idx++);
        this.priceExp = args.get(idx++);
        this.termType = args.get(idx++);
        this.delaySecs = Double.parseDouble(args.get(idx++));
        this.periodSecs = Double.parseDouble(args.get(idx++));
        this.totalDurationSecs = Double.parseDouble(args.get(idx));
        try {
            this.quantity = Integer.parseInt(quantityExp);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Wrong quantity value [" + quantityExp + "] in LOANS->outgoing rule. Must be integer", e);
        }
        try {
            this.rate = Double.parseDouble(rateExp);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Wrong rate value [" + rateExp + "] in LOANS->outgoing rule. Must be double", e);
        }
        try {
            this.price = Double.parseDouble(priceExp);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Wrong price value [" + priceExp + "] in LOANS->outgoing rule. Must be double", e);
        }

    }

    private void splitExpressionAndLoad(String exp, List<String> set) {
        String[] arr = exp.split("\\|");
        for (String str : arr) {
            set.add(str.trim());
        }
    }

    public String getPartyRole() {
        return partyRole;
    }

    public List<String> getCounterparties() {
        return counterparties;
    }

    public List<String> getSecurities() {
        return securities;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getRate() {
        return rate;
    }

    public Double getPrice() {
        return price;
    }

    public String getTermType() {
        return termType;
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
