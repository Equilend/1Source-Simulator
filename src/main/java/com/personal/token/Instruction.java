package com.personal.token;

import java.util.List;

public class Instruction {
    private String settlementBic;
    private String localAgentBic;
    private String localAgentName;
    private String localAgentAcct;
    private List<LocalMarketFields> localMarketFields;

    
    public Instruction(String settlementBic, String localAgentBic, String localAgentName, String localAgentAcct,
            List<LocalMarketFields> localMarketFields) {
        this.settlementBic = settlementBic;
        this.localAgentBic = localAgentBic;
        this.localAgentName = localAgentName;
        this.localAgentAcct = localAgentAcct;
        this.localMarketFields = localMarketFields;
    }
    public List<LocalMarketFields> getLocalMarketFields() {
        return localMarketFields;
    }
    public void setLocalMarketFields(List<LocalMarketFields> localMarketFields) {
        this.localMarketFields = localMarketFields;
    }
    public String getSettlementBic() {

        return settlementBic;
    }
    public void setSettlementBic(String settlementBic) {
        this.settlementBic = settlementBic;
    }
    public String getLocalAgentBic() {
        return localAgentBic;
    }
    public void setLocalAgentBic(String localAgentBic) {
        this.localAgentBic = localAgentBic;
    }
    public String getLocalAgentName() {
        return localAgentName;
    }
    public void setLocalAgentName(String localAgentName) {
        this.localAgentName = localAgentName;
    }
    public String getLocalAgentAcct() {
        return localAgentAcct;
    }
    public void setLocalAgentAcct(String localAgentAcct) {
        this.localAgentAcct = localAgentAcct;
    }

    
}
