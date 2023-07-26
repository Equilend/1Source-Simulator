package com.personal.token;

public class LocalMarketFields {
    private String localFieldName;
    private String localFieldValue;
    
    
    public LocalMarketFields(String localFieldName, String localFieldValue) {
        this.localFieldName = localFieldName;
        this.localFieldValue = localFieldValue;
    }
    public String getLocalFieldName() {
        return localFieldName;
    }
    public void setLocalFieldName(String localFieldName) {
        this.localFieldName = localFieldName;
    }
    public String getLocalFieldValue() {
        return localFieldValue;
    }
    public void setLocalFieldValue(String localFieldValue) {
        this.localFieldValue = localFieldValue;
    }
   
   
}
