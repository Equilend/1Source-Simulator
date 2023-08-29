package com.equilend.simulator.API;

public class ContractProposalResponse {

    private String resourceUri;

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getContractId() {
        String uri = resourceUri;
        String[] arr = uri.split("/");
        String contractId = arr[arr.length-1];

        return contractId;
    }

    @Override
    public String toString() {
        return resourceUri;
    }

}