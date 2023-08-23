package com.equilend.simulator.API;

public class ContractProposalResponse {
    private String resourceUri;


    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getContractId(){
        return this.resourceUri.substring(21);
    }

    @Override
    public String toString(){
        return resourceUri;
    }
}
