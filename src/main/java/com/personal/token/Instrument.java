package com.personal.token;

public class Instrument {
    private String ticker;
    private String cusip;
    private String isin;
    private String sedol;
    private String quick;
    private String figi;
    private String description;
    private Price price;
    

    
    public Instrument(String ticker, String cusip, String isin, String sedol, String figi, String description) {
        this.ticker = ticker;
        this.cusip = cusip;
        this.isin = isin;
        this.sedol = sedol;
        this.figi = figi;
        this.description = description;
    }
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public String getCusip() {
        return cusip;
    }
    public void setCusip(String cusip) {
        this.cusip = cusip;
    }
    public String getIsin() {
        return isin;
    }
    public void setIsin(String isin) {
        this.isin = isin;
    }
    public String getSedol() {
        return sedol;
    }
    public void setSedol(String sedol) {
        this.sedol = sedol;
    }
    public String getQuick() {
        return quick;
    }
    public void setQuick(String quick) {
        this.quick = quick;
    }
    public String getFigi() {
        return figi;
    }
    public void setFigi(String figi) {
        this.figi = figi;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Price getPrice() {
        return price;
    }
    public void setPrice(Price price) {
        this.price = price;
    }
    
    
}
