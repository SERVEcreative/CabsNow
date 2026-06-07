package com.servecreative.WholeProject.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_event_log", indexes = {
        @Index(name = "idx_ride_event_duty", columnList = "duty_id"),
        @Index(name = "idx_ride_event_id", columnList = "event_id", unique = true)
})
public class RideEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "duty_id", nullable = false)
    private int dutyId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public int getDutyId() { return dutyId; }
    public void setDutyId(int dutyId) { this.dutyId = dutyId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
