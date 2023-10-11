package com.equilend.simulator.rerate;

import com.equilend.simulator.trade.rate.Rate;

public class RerateProposal {

    Rate rate;

    public RerateProposal(Rate rate) {
        this.rate = rate;
    }

    public Rate getRate() {
        return rate;
    }

    public void setRate(Rate rate) {
        this.rate = rate;
    }
 
}