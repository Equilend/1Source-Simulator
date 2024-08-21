package com.equilend.simulator.configurator.rules.rerate_rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.equilend.simulator.api.FedAPIException;
import com.equilend.simulator.configurator.rules.RuleValidator;
import com.os.client.model.Contract;
import com.os.client.model.FeeRate;
import com.os.client.model.FixedRate;
import com.os.client.model.FloatingRate;
import com.os.client.model.OneOfRebateRateRebate;
import com.os.client.model.Rate;
import com.os.client.model.RebateRate;
import com.os.client.model.TradeAgreement;
import com.os.client.model.TransactingParty;

public class RerateProposeRule implements RerateRule {

    private String counterpartyExp;
    private final Set<String> counterparties = new HashSet<>();
    private String securityExp;
    private final Set<String> securities = new HashSet<>();
    private String rateExp;
    private final Set<String> rates = new HashSet<>();
    private Boolean propose = null;
    private Double delta;
    private Double delay;

    public RerateProposeRule(String rule) {
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
        this.delta = Double.parseDouble(args.get(idx++));
        propose = args.get(idx++).equals("P");
        this.delay = Double.parseDouble(args.get(idx));
    }

    private void splitExpressionAndLoad(String exp, Set<String> set) {
        String[] arr = exp.split("\\|");
        for (String str : arr) {
            set.add(str.trim());
        }
    }


    public Double getDelta() {
        return delta;
    }

    public Double getDelay() {
        return delay;
    }

    public boolean shouldPropose() {
        return propose;
    }

    private String getTradeCptyId(TradeAgreement trade, String partyId) {
        for (TransactingParty tp : trade.getTransactingParties()) {
            if (!tp.getParty().getPartyId().equals(partyId)) {
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean isApplicable(Contract contract, String partyId) throws FedAPIException {
        TradeAgreement trade = contract.getTrade();
        String cpty = getTradeCptyId(trade, partyId);
        
        boolean rebate = true;
        
        Rate loanRate = trade.getRate();
        Double rate = null;
		Double effectiveRate = null;
		if (loanRate instanceof RebateRate) {
			OneOfRebateRateRebate oneOfRebateRateRebate = ((RebateRate) loanRate).getRebate();
			if (oneOfRebateRateRebate instanceof FloatingRate) {
				rate = ((FloatingRate) oneOfRebateRateRebate).getFloating().getSpread();
				effectiveRate = ((FloatingRate) oneOfRebateRateRebate).getFloating().getEffectiveRate();
			} else if (oneOfRebateRateRebate instanceof FixedRate) {
				rate = ((FixedRate) oneOfRebateRateRebate).getFixed().getBaseRate();
				effectiveRate = ((FloatingRate) oneOfRebateRateRebate).getFloating().getEffectiveRate();
			}
		} else if (loanRate instanceof FeeRate) {
			rebate = false;
			rate = ((FeeRate) loanRate).getFee().getBaseRate();
			effectiveRate = ((FeeRate) loanRate).getFee().getEffectiveRate();
		}
        
        return RuleValidator.validCounterparty(counterparties, cpty) &&
            RuleValidator.validSecurity(securities, trade.getInstrument())
            && RuleValidator.validRate(rates, effectiveRate, trade.getInstrument().getSedol(),
            rebate);
    }

    @Override
    public String toString() {
        if (propose != null) {
            if (propose) {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp + "}, PROPOSE, DELTA{"
                    + delta + "}, DELAY{" + delay + "}";
            } else {
                return "CPTY{" + counterpartyExp + "}, SEC{" + securityExp + "}, QTY{" + rateExp + "}, IGNORE, DELTA{"
                    + delta + "}, DELAY{" + delay + "}";
            }
        }
        return "";
    }

}