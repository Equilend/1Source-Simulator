package com.equilend.simulator.trade.rate;

public class Rate {
    
    private RebateRate rebate;
    private FixedRate fee;
    
    public Rate(RebateRate rebate) {
        this.rebate = rebate;
    }

    public Rate(FixedRate fee) {
        this.fee = fee;
    }

    public RebateRate getRebate() {
        return rebate;
    }
    
    public void setRebate(RebateRate rebate) {
        this.rebate = rebate;
    }
    
    public FixedRate getFee() {
        return fee;
    }
    
    public void setFee(FixedRate fee) {
        this.fee = fee;
    }

    public Double getEffectiveRate(){
        if (fee != null){
            return fee.getEffectiveRate();
        }
        else if (rebate != null){
           if (rebate.getFixed() != null) {
                return rebate.getFixed().getEffectiveRate();
           }
           else if (rebate.getFloating() != null){
                return rebate.getFloating().getEffectiveRate();
           }
        }
        return -1.0;

    }

}