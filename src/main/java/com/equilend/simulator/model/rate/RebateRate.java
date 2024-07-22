package com.equilend.simulator.model.rate;

public class RebateRate {

    private FixedRateDef fixed = null;
    private FloatingRateDef floating = null;

    public RebateRate(FixedRateDef fixed) {
        this.fixed = fixed;
    }

    public RebateRate(FloatingRateDef floating) {
        this.floating = floating;
    }

    public FixedRateDef getFixed() {
        return fixed;
    }

    public void setFixed(FixedRateDef fixed) {
        this.fixed = fixed;
    }

    public FloatingRateDef getFloating() {
        return floating;
    }

    public void setFloating(FloatingRateDef floating) {
        this.floating = floating;
    }

}