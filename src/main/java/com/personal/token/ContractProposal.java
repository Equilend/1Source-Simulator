package com.personal.token;

import java.util.List;

public class ContractProposal {
    private Trade trade;
    private List<Settlement> settlement;

    
    public ContractProposal(Trade trade, List<Settlement> settlement) {
        this.trade = trade;
        this.settlement = settlement;
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
