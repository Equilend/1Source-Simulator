package com.equilend.simulator.model.rate;

import com.equilend.simulator.api.FedAPIConnector;
import com.equilend.simulator.api.FedAPIException;

public class Rate {

    private RebateRate rebate = null;
    private FixedRateDef fee = null;

    public Rate(RebateRate rebate) {
        this.rebate = rebate;
    }

    public Rate(FixedRateDef fee) {
        this.fee = fee;
    }

    public RebateRate getRebate() {
        return rebate;
    }

    public void setRebate(RebateRate rebate) {
        this.rebate = rebate;
    }

    public FixedRateDef getFee() {
        return fee;
    }

    public void setFee(FixedRateDef fee) {
        this.fee = fee;
    }

    public Double getEffectiveRate() throws FedAPIException {
        if (fee != null) {
            return fee.getBaseRate();
        } else if (rebate != null) {
            if (rebate.getFixed() != null) {
                return rebate.getFixed().getBaseRate();
            } else if (rebate.getFloating() != null) {
                String benchmarkStr = rebate.getFloating().getBenchmark().name();
                Double benchmarkRate = FedAPIConnector.getRefRate(benchmarkStr).getPercentRate();
                return benchmarkRate + rebate.getFloating().getSpread();
            }
        }
        return -1.0;

    }

}