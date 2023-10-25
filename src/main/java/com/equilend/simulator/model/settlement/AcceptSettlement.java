package com.equilend.simulator.model.settlement;

import com.equilend.simulator.model.trade.transacting_party.PartyRole;

public class AcceptSettlement {

    private Settlement settlement;
    private Integer roundingRule;
    private String roundingMode;
    
    public AcceptSettlement(Settlement settlement, PartyRole role) {
        this.settlement = settlement;
        if (role == PartyRole.LENDER){
            roundingRule = 10;
            roundingMode = "ALWAYSUP";
        }
    }
    
    public Settlement getSettlement() {
        return settlement;
    }
    
    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
    }

    public int getRoundingRule() {
        return roundingRule;
    }

    public void setRoundingRule(int roundingRule) {
        this.roundingRule = roundingRule;
    }

    public String getRoundingMode() {
        return roundingMode;
    }

    public void setRoundingMode(String roundingMode) {
        this.roundingMode = roundingMode;
    }

}