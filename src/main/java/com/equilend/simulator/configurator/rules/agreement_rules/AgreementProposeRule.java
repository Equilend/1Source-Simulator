package com.equilend.simulator.configurator.rules.agreement_rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.configurator.rules.RuleValidator;
import com.os.client.model.TransactingParty;
import com.os.client.model.VenueTradeAgreement;

public class AgreementProposeRule implements AgreementRule {

    private String counterpartyExp;
    private final Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private final Set<String> securities = new HashSet<>();
    private String quantityExp;
    private final Set<String> quantities = new HashSet<>();
    private boolean ignore;
    private Double delay;

    public AgreementProposeRule(String rule) {
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
        ignore = args.get(idx++).equals("I");
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

    public boolean shouldIgnore() {
        return ignore;
    }

    public String getTradeCptyId(VenueTradeAgreement venueTradeAgreement, String partyId) {
        for (TransactingParty tp : venueTradeAgreement.getTransactingParties()) {
            if (!tp.getParty().getPartyId().equals(partyId)) {
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isApplicable(VenueTradeAgreement venueTradeAgreement, String partyId) {
        String cpty = getTradeCptyId(venueTradeAgreement, partyId);
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, venueTradeAgreement.getInstrument())
            && RuleValidator.validQuantity(quantities, venueTradeAgreement.getQuantity());
    }

    @Override
    public String toString() {
        if (ignore) {
            return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp + "}, IGNORE, DELAY{"
                + delay + "}";
        } else {
            return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + quantityExp + "}, PROPOSE, DELAY{"
                + delay + "}";
        }
    }

}