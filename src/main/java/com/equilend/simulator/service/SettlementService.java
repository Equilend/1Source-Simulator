package com.equilend.simulator.service;

import com.os.client.model.PartyRole;
import com.os.client.model.PartySettlementInstruction;
import com.os.client.model.SettlementInstruction;

public class SettlementService {

    public static PartySettlementInstruction createPartySettlementInstruction(PartyRole role) {
        SettlementInstruction settlementInstruction = new SettlementInstruction()
            .settlementBic("DTCYUS33")
            .localAgentBic("IRVTBEBBXXX")
            .localAgentName("THE BANK OF NEW YORK MELLON SA/NV")
            .localAgentAcct("AGNT12345")
            .dtcParticipantNumber("0901");
        PartySettlementInstruction partySettlementInstruction = new PartySettlementInstruction().instruction(
            settlementInstruction).partyRole(role).internalAcctCd("SSIXXX");
        return partySettlementInstruction;
    }

}
