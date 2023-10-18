package com.equilend.simulator.model.trade.rate;

public class RebateRate {
 
    private FixedRate fixed = null;
    private FloatingRate floating = null;
    
    public RebateRate(FixedRate fixed) {
        this.fixed = fixed;
    }
    
    public RebateRate(FloatingRate floating) {
        this.floating = floating;
    }

    public FixedRate getFixed() {
        return fixed;
    }

    public void setFixed(FixedRate fixed) {
        this.fixed = fixed;
    }

    public FloatingRate getFloating() {
        return floating;
    }

    public void setFloating(FloatingRate floating) {
        this.floating = floating;
    }

}