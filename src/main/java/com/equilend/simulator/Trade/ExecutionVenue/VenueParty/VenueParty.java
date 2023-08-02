package com.equilend.simulator.Trade.ExecutionVenue.VenueParty;

import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class VenueParty {
    private PartyRole partyRole;
    private String venuePartyId;
    private InternalRefRequest internalRef;
    
    public VenueParty(PartyRole partyRole) {
        this.partyRole = partyRole;
    }
    public PartyRole getPartyRole() {
        return partyRole;
    }
    public void setPartyRole(PartyRole partyRole) {
        this.partyRole = partyRole;
    }
    public String getVenuePartyId() {
        return venuePartyId;
    }
    public void setVenuePartyId(String venuePartyId) {
        this.venuePartyId = venuePartyId;
    }
    public InternalRefRequest getInternalRef() {
        return internalRef;
    }
    public void setInternalRef(InternalRefRequest internalRef) {
        this.internalRef = internalRef;
    }

    
}
