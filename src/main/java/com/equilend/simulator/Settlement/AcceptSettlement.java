package com.equilend.simulator.Settlement;

public class AcceptSettlement {
    Settlement borrowerSettlementInformation;

    public AcceptSettlement(Settlement borrowerSettlementInformation) {
        this.borrowerSettlementInformation = borrowerSettlementInformation;
    }

    public Settlement getBorrowerSettlementInformation() {
        return borrowerSettlementInformation;
    }

    public void setBorrowerSettlementInformation(Settlement borrowerSettlementInformation) {
        this.borrowerSettlementInformation = borrowerSettlementInformation;
    }

}
