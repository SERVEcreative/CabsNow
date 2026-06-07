package com.servecreative.WholeProject.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RideDispatchMessage {

    private String eventId;
    private String eventType;
    private int dutyId;
    private List<String> destinations;
    private RideStatusEvent payload;
    private long publishedAtEpochMs;

    public RideDispatchMessage() {}

    public static RideDispatchMessage of(String eventType, int dutyId, RideStatusEvent payload, List<String> destinations) {
        RideDispatchMessage message = new RideDispatchMessage();
        message.eventId = UUID.randomUUID().toString();
        message.eventType = eventType;
        message.dutyId = dutyId;
        message.payload = payload;
        message.destinations = new ArrayList<>(destinations);
        message.publishedAtEpochMs = System.currentTimeMillis();
        return message;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public int getDutyId() { return dutyId; }
    public void setDutyId(int dutyId) { this.dutyId = dutyId; }
    public List<String> getDestinations() { return destinations; }
    public void setDestinations(List<String> destinations) { this.destinations = destinations; }
    public RideStatusEvent getPayload() { return payload; }
    public void setPayload(RideStatusEvent payload) { this.payload = payload; }
    public long getPublishedAtEpochMs() { return publishedAtEpochMs; }
    public void setPublishedAtEpochMs(long publishedAtEpochMs) { this.publishedAtEpochMs = publishedAtEpochMs; }
}
