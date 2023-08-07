package com.equilend.simulator.Settlement;

import com.equilend.simulator.Settlement.Instruction.Instruction;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class Settlement {
    private PartyRole partyRole;
    private Instruction instruction;
    
    
    public Settlement(PartyRole partyRole, Instruction instruction) {
        this.partyRole = partyRole;
        this.instruction = instruction;
    }
    public PartyRole getPartyRole() {
        return partyRole;
    }
    public void setPartyRole(PartyRole partyRole) {
        this.partyRole = partyRole;
    }
    public Instruction getInstruction() {
        return instruction;
    }
    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public String toString(){
        return partyRole.name();
    }
}
