package com.equilend.simulator.Trade.Rate;

import java.math.BigDecimal;

public class Rate {
    private BigDecimal rebateBps;
    private Float rebateSreadBps;
    private BenchmarkCd benchmarkCd;
    private Float feeBps;
    

    public Rate(BigDecimal rebateBps) {
        this.rebateBps = rebateBps;
    }
    public BigDecimal getRebateBps() {
        return rebateBps;
    }
    public void setRebateBps(BigDecimal rebateBps) {
        this.rebateBps = rebateBps;
    }
    public Float getRebateSreadBps() {
        return rebateSreadBps;
    }
    public void setRebateSreadBps(Float rebateSreadBps) {
        this.rebateSreadBps = rebateSreadBps;
    }
    public BenchmarkCd getBenchmarkCd() {
        return benchmarkCd;
    }
    public void setBenchmarkCd(BenchmarkCd benchmarkCd) {
        this.benchmarkCd = benchmarkCd;
    }
    public Float getFeeBps() {
        return feeBps;
    }
    public void setFeeBps(Float feeBps) {
        this.feeBps = feeBps;
    }
    
}
