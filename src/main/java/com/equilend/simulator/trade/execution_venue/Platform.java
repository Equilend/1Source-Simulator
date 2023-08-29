package com.equilend.simulator.trade.execution_venue;

public class Platform {
    
    private String gleifLei;
    private String legalName;
    private String venueName;
    private String venueRefId;

    public Platform(String gleifLei, String legalName, String venueName, String venueRefId) {
        this.gleifLei = gleifLei;
        this.legalName = legalName;
        this.venueName = venueName;
        this.venueRefId = venueRefId;
    }

    public String getGleifLei() {
        return gleifLei;
    }
    
    public void setGleifLei(String gleifLei) {
        this.gleifLei = gleifLei;
    }
    
    public String getLegalName() {
        return legalName;
    }
    
    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getVenueName() {
        return venueName;
    }
    
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    
    public String getVenueRefId() {
        return venueRefId;
    }

    public void setVenueRefId(String venueRefId) {
        this.venueRefId = venueRefId;
    }

}