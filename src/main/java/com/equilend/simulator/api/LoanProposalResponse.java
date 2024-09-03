package com.equilend.simulator.api;

public class LoanProposalResponse {

    private String resourceUri;

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getLoanId() {
        String uri = resourceUri;
        String[] arr = uri.split("/");

        return arr[arr.length - 1];
    }

    @Override
    public String toString() {
        return resourceUri;
    }

}