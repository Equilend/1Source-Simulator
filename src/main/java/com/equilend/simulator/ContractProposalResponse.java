package com.equilend.simulator;

public class ContractProposalResponse {
    private String resourceUri;

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    @Override
    public String toString(){
        return resourceUri;
    }
}
