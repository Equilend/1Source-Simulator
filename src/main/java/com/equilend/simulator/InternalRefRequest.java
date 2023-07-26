package com.equilend.simulator;

public class InternalRefRequest {
    private String brokerCd;
    private String accountId;
    private String internalRefId;

    public String getBrokerCd() {
        return brokerCd;
    }
    public void setBrokerCd(String brokerCd) {
        this.brokerCd = brokerCd;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public String getInternalRefId() {
        return internalRefId;
    }
    public void setInternalRefId(String internalRefId) {
        this.internalRefId = internalRefId;
    }
}
