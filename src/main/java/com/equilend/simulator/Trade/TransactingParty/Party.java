package com.equilend.simulator.Trade.TransactingParty;

public class Party {

    private String partyId;
    private String partyName;
    private String gleifLei;

    public Party(String partyId, String partyName, String gleifLei) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.gleifLei = gleifLei;
    }
    
    public String getPartyId() {
        return partyId;
    }
    
    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
    
    public String getPartyName() {
        return partyName;
    }
    
    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }
    
    public String getGleifLei() {
        return gleifLei;
    }
    
    public void setGleifLei(String gleifLei) {
        this.gleifLei = gleifLei;
    }
    
    @Override
    public String toString(){
        return this.partyId + " (" + this.gleifLei + ")";
    }
    
}