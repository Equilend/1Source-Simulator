package com.equilend.simulator.model.trade.rate;

public class FixedRate {

    private Double baseRate;
    private Double effectiveRate;
    private String effectiveDate;
    private String cutoffTime;

    public FixedRate(Double baseRate, Double effectiveRate, String effectiveDate, String cutoffTime) {
        this.baseRate = baseRate;
        this.effectiveRate = effectiveRate;
        this.effectiveDate = effectiveDate;
        this.cutoffTime = cutoffTime;
    }

    public Double getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(Double baseRate) {
        this.baseRate = baseRate;
    }

    public Double getEffectiveRate() {
        return effectiveRate;
    }

    public void setEffectiveRate(Double effectiveRate) {
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