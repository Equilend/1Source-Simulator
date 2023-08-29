package com.equilend.simulator.settlement;

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