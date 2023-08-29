package com.equilend.simulator.Trade.TransactingParty;

public class TransactingParty {
    
    private PartyRole partyRole;
    private Party party;

    public PartyRole getPartyRole() {
        return partyRole;
    }
    
    public void setPartyRole(PartyRole partyRole) {
        this.partyRole = partyRole;
    }
    
    public Party getParty() {
        return party;
    }
    
    public void setParty(Party party) {
        this.party = party;
    }
    
}