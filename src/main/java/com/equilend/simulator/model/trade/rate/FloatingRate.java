package com.equilend.simulator.model.trade.rate;

import java.math.BigDecimal;

public class FloatingRate {

    private BenchmarkCd benchmark;
    private Double baseRate;
    private Double spread;
    private Double effectiveRate;
    private Boolean isAutoRerate;
    private BigDecimal effectiveDateDelay;
    private String effectiveDate;
    private String cutoffTime;

    public FloatingRate(BenchmarkCd benchmark, Double baseRate, Double spread, Double effectiveRate, Boolean isAutoRerate,
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

    public Double getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(Double baseRate) {
        this.baseRate = baseRate;
    }

    public Double getSpread() {
        return spread;
    }

    public void setSpread(Double spread) {
        this.spread = spread;
    }

    public Double getEffectiveRate() {
        return effectiveRate;
    }

    public void setEffectiveRate(Double effectiveRate) {
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