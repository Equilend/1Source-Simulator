package com.equilend.simulator.trade.rate;

import java.math.BigDecimal;

public class FloatingRate {

    private BenchmarkCd benchmark;
    private Float baseRate;
    private Float spread;
    private Float effectiveRate;
    private Boolean isAutoRerate;
    private BigDecimal effectiveDateDelay;
    private String effectiveDate;
    private String cutoffTime;

    public FloatingRate(BenchmarkCd benchmark, Float baseRate, Float spread, Float effectiveRate, Boolean isAutoRerate,
            BigDecimal effectiveDateDelay, String effectiveDate, String cutoffTime) {
        this.benchmark = benchmark;
        this.baseRate = baseRate;
        this.spread = spread;
        this.effectiveRate = effectiveRate;
        this.isAutoRerate = isAutoRerate;
        this.effectiveDateDelay = effectiveDateDelay;
        this.effectiveDate = effectiveDate;
        this.cutoffTime = cutoffTime;
    }

    public BenchmarkCd getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(BenchmarkCd benchmark) {
        this.benchmark = benchmark;
    }

    public Float getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(Float baseRate) {
        this.baseRate = baseRate;
    }

    public Float getSpread() {
        return spread;
    }

    public void setSpread(Float spread) {
        this.spread = spread;
    }

    public Float getEffectiveRate() {
        return effectiveRate;
    }

    public void setEffectiveRate(Float effectiveRate) {
        this.effectiveRate = effectiveRate;
    }

    public Boolean getIsAutoRerate() {
        return isAutoRerate;
    }

    public void setIsAutoRerate(Boolean isAutoRerate) {
        this.isAutoRerate = isAutoRerate;
    }

    public BigDecimal getEffectiveDateDelay() {
        return effectiveDateDelay;
    }

    public void setEffectiveDateDelay(BigDecimal effectiveDateDelay) {
        this.effectiveDateDelay = effectiveDateDelay;
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