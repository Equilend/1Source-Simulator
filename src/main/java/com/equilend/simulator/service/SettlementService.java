package com.equilend.simulator.service;

import java.util.Properties;

import com.equilend.simulator.configurator.Config;
import com.os.client.model.PartyRole;
import com.os.client.model.PartySettlementInstruction;
import com.os.client.model.SettlementInstruction;

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
