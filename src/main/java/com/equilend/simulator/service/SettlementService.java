package com.equilend.simulator.service;

import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.settlement.PartySettlementInstruction;
import com.equilend.simulator.model.settlement.instruction.SettlementInstruction;

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
