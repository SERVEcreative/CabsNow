package com.servecreative.WholeProject.DTO;

public class RideDispatchEnvelope {

    private String destination;
    private RideStatusEvent payload;

    public RideDispatchEnvelope() {}

    public RideDispatchEnvelope(String destination, RideStatusEvent payload) {
        this.destination = destination;
        this.payload = payload;
    }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public RideStatusEvent getPayload() { return payload; }
    public void setPayload(RideStatusEvent payload) { this.payload = payload; }
}
