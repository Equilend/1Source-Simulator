package com.equilend.simulator.trade.rate;

public class FixedRate {

    private Float baseRate;
    private Float effectiveRate;
    private String effectiveDate;
    private String cutoffTime;

    public FixedRate(Float baseRate, Float effectiveRate, String effectiveDate, String cutoffTime) {
        this.baseRate = baseRate;
        this.effectiveRate = effectiveRate;
        this.effectiveDate = effectiveDate;
        this.cutoffTime = cutoffTime;
    }

    public Float getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(Float baseRate) {
        this.baseRate = baseRate;
    }

    public Float getEffectiveRate() {
        return effectiveRate;
    }

    public void setEffectiveRate(Float effectiveRate) {
        this.effectiveRate = effectiveRate;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getCutoffTime() {
        return cutoffTime;
    }

    public void setCutoffTime(String cutoffTime) {
        this.cutoffTime = cutoffTime;
    }
    
}