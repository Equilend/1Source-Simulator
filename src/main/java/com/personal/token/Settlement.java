package com.personal.token;

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

    
}
