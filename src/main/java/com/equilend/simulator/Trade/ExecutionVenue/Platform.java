package com.equilend.simulator.Trade.ExecutionVenue;

// import java.time.LocalDateTime;

public class Platform {
    private String gleifLei;
    private String legalName;
    // private String mic;
    private String venueName;
    private String venueRefId;
    // private LocalDateTime transactionDateTime;

    public Platform(String gleifLei, String legalName, String venueName, String venueRefId) {
        this.gleifLei = gleifLei;
        this.legalName = legalName;
        // this.mic = mic;
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
    // public String getMic() {
    //     return mic;
    // }
    // public void setMic(String mic) {
    //     this.mic = mic;
    // }
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
    // public LocalDateTime getTransactionDateTime() {
    //     return transactionDateTime;
    // }
    // public void setTransactionDateTime(LocalDateTime transactionDateTime) {
    //     this.transactionDateTime = transactionDateTime;
    // }

    
}
