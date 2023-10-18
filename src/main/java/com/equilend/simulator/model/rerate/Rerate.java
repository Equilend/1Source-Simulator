package com.equilend.simulator.model.rerate;

import java.util.List;

import com.equilend.simulator.model.trade.rate.Rate;
import com.equilend.simulator.model.trade.transacting_party.PartyRole;
import com.equilend.simulator.model.trade.transacting_party.TransactingParty;

public class Rerate {
    
    private String rerateId;    
    private String loanId;
    private String status;
    private Rate rate;
    private Rate rerate;
    private String lastUpdateDatetime;
    private List<TransactingParty> transactingParties;
    
    public String getRerateId() {
        return rerateId;
    }
    
    public void setRerateId(String rerateId) {
        this.rerateId = rerateId;
    }
    
    public String getLoanId() {
        return loanId;
    }
    
    public void setLoanId(String loanId) {
        this.loanId = loanId;
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

    public List<TransactingParty> getTransactingParties() {
        return transactingParties;
    }

    public void setTransactingParties(List<TransactingParty> transactingParties) {
        this.transactingParties = transactingParties;
    }

    public PartyRole getPartyRole(String partyId) {
        for (TransactingParty tp : transactingParties){
            if (tp.getParty().getPartyId().equals(partyId)){
                return tp.getPartyRole();
            }
        }
        return null;
    }

}
