package com.equilend.simulator.trade.execution_venue;

import java.util.List;

import com.equilend.simulator.trade.execution_venue.venue_party.VenueParty;

public class ExecutionVenue {
    
    private VenueType type;
    private Platform platform;
    private List<VenueParty> venueParties;

    public ExecutionVenue(VenueType type, Platform platform, List<VenueParty> venueParties) {
        this.type = type;
        this.platform = platform;
        this.venueParties = venueParties;
    }
    
    public VenueType getType() {
        return type;
    }
    
    public void setType(VenueType type) {
        this.type = type;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
    
    public List<VenueParty> getVenueParties() {
        return venueParties;
    }
    
    public void setVenueParties(List<VenueParty> venueParties) {
        this.venueParties = venueParties;
    }
   
}