package com.equilend.simulator.model.event;

public class Event {

    private int eventId;
    private String eventType;
    private String eventDateTime;
    private String resourceUri;
    
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(String eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getResourceId(){
        if (this.resourceUri == null) return "";
        String[] arr = this.resourceUri.split("/");
        String contractId = arr[arr.length-1];

        return contractId;
    }

    @Override
    public String toString(){
        return String.format("[%s] %s @%s (%s)", getEventType(), getEventId(), getEventDateTime(), getResourceUri());
    }

}