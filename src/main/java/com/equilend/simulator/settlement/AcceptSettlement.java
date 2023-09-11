package com.equilend.simulator.settlement;

public class AcceptSettlement {

    private Settlement settlement;

    public AcceptSettlement(Settlement settlement) {
        this.settlement = settlement;
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
    }

}