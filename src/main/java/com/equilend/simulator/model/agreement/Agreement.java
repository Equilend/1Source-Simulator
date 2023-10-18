package com.equilend.simulator.model.agreement;

import com.equilend.simulator.model.trade.Trade;

public class Agreement {

    private String agreementId;
    private Trade trade;
    
    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public Trade getTrade() {
        return trade;
    }
    
    public void setTrade(Trade trade) {
        this.trade = trade;
    }

}