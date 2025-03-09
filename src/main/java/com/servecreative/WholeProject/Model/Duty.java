package com.servecreative.WholeProject.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "duties")
public class Duty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int dutyId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private DutyStatus status;  // Status of the duty (e.g., PENDING, ACCEPTED)

    @ManyToOne
    @JoinColumn(name = "rider_id", referencedColumnName = "riderId", nullable = false)
    @JsonBackReference // Prevents infinite recursion by marking this as the back-reference side
    private Rider rider; // Links to the Rider who created the duty

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "driverId", nullable = true)
    private Driver assignedDriver; // The driver assigned to the duty, if any

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Timestamp for duty creation

    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation; // Pickup location for the ride

    @Column(name = "drop_location", nullable = false)
    private String dropLocation; // Drop location for the ride


    // Getters and Setters
    public int getDutyId() {
        return dutyId;
    }

    public void setDutyId(int dutyId) {
        this.dutyId = dutyId;
    }

    public DutyStatus getStatus() {
        return status;
    }

    public void setStatus(DutyStatus status) {
        this.status = status;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public Driver getAssignedDriver() {
        return assignedDriver;
    }

    public void setAssignedDriver(Driver assignedDriver) {
        this.assignedDriver = assignedDriver;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public enum DutyStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        COMPLETED

    }
}
