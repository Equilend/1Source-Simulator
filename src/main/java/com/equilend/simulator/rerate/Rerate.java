package com.equilend.simulator.rerate;

import com.equilend.simulator.trade.rate.Rate;

public class Rerate {
    
    private String rerateId;    
    private String contractId;
    private String status;
    private Rate rate;
    private Rate rerate;
    private String lastUpdateDatetime;
    
    public String getRerateId() {
        return rerateId;
    }
    
    public void setRerateId(String rerateId) {
        this.rerateId = rerateId;
    }
    
    public String getContractId() {
        return contractId;
    }
    
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Rate getRate() {
        return rate;
    }
    
    public void setRate(Rate rate) {
        this.rate = rate;
    }
    
    public Rate getRerate() {
        return rerate;
    }
    
    public void setRerate(Rate rerate) {
        this.rerate = rerate;
    }
    
    public String getLastUpdateDatetime() {
        return lastUpdateDatetime;
    }

    public void setLastUpdateDatetime(String lastUpdateDatetime) {
        this.lastUpdateDatetime = lastUpdateDatetime;
    }

}
