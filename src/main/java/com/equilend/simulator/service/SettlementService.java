package com.equilend.simulator.service;

import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.settlement.PartySettlementInstruction;
import com.equilend.simulator.model.settlement.instruction.SettlementInstruction;
import java.util.Properties;

public class SettlementService {

    public static PartySettlementInstruction createPartySettlementInstruction(PartyRole role) {
        Properties props = Config.getInstance().getProperties();
        SettlementInstruction settlementInstruction = new SettlementInstruction()
            .settlementBic(props.getProperty("bot.settlement_bic"))
            .localAgentBic(props.getProperty("bot.local_agent_bic"))
            .localAgentName(props.getProperty("bot.local_agent_name"))
            .localAgentAcct(props.getProperty("bot.local_agent_acct"))
            .dtcParticipantNumber(props.getProperty("bot.dtc_partipant_number"));
        PartySettlementInstruction partySettlementInstruction = new PartySettlementInstruction().instruction(
            settlementInstruction).partyRole(role).internalAcctCd("internalAcctCd");
        return partySettlementInstruction;
    }

}
