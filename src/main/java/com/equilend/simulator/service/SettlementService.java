package com.equilend.simulator.service;

import com.os.client.model.PartyRole;
import com.os.client.model.PartySettlementInstruction;
import com.os.client.model.SettlementInstruction;

public class SettlementService {

    public static PartySettlementInstruction createPartySettlementInstruction(PartyRole role) {
        SettlementInstruction settlementInstruction = new SettlementInstruction()
            .settlementBic("XXXXXXXX")
            .localAgentBic("YYYYYYYY")
            .localAgentName("ZZZ Clearing")
            .localAgentAcct("2468999");
        PartySettlementInstruction partySettlementInstruction = new PartySettlementInstruction().instruction(
            settlementInstruction).partyRole(role);
        return partySettlementInstruction;
    }

}
