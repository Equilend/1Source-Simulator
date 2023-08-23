package com.equilend.simulator.Contract;

import java.util.List;

import com.equilend.simulator.Settlement.Settlement;
import com.equilend.simulator.Trade.Trade;

public class Contract {
    private String contractId;
    private String contractStatus;
    private int lastEventId;
    private Trade trade;
    private List<Settlement> settlement;

    public String getContractId() {
        return contractId;
    }
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
    public String getContractStatus() {
        return contractStatus;
    }
    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }
    public int getLastEventId() {
        return lastEventId;
    }
    public void setLastEventId(int lastEventId) {
        this.lastEventId = lastEventId;
    }
    public Trade getTrade() {
        return trade;
    }
    public void setTrade(Trade trade) {
        this.trade = trade;
    }
    public List<Settlement> getSettlement() {
        return settlement;
    }
    public void setSettlement(List<Settlement> settlement) {
        this.settlement = settlement;
    }
}
