package com.servecreative.WholeProject.DTO;

public class RideStatusEvent {
    private int dutyId;
    private int riderId;
    private Integer driverId;
    private String status;
    private String message;
    private String pickupLocation;
    private String dropLocation;
    private double fare;
    private String vehicleType;
    private String driverName;
    private Long acceptDeadlineEpochMs;
    private long timestampEpochMs;
    private Integer nearbyDriversNotified;

    public RideStatusEvent() {}

    public RideStatusEvent(int dutyId, int riderId, Integer driverId, String status, String message) {
        this.dutyId = dutyId;
        this.riderId = riderId;
        this.driverId = driverId;
        this.status = status;
        this.message = message;
        this.timestampEpochMs = System.currentTimeMillis();
    }

    public int getDutyId() { return dutyId; }
    public void setDutyId(int dutyId) { this.dutyId = dutyId; }
    public int getRiderId() { return riderId; }
    public void setRiderId(int riderId) { this.riderId = riderId; }
    public Integer getDriverId() { return driverId; }
    public void setDriverId(Integer driverId) { this.driverId = driverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }
    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public Long getAcceptDeadlineEpochMs() { return acceptDeadlineEpochMs; }
    public void setAcceptDeadlineEpochMs(Long acceptDeadlineEpochMs) { this.acceptDeadlineEpochMs = acceptDeadlineEpochMs; }
    public long getTimestampEpochMs() { return timestampEpochMs; }
    public void setTimestampEpochMs(long timestampEpochMs) { this.timestampEpochMs = timestampEpochMs; }
    public Integer getNearbyDriversNotified() { return nearbyDriversNotified; }
    public void setNearbyDriversNotified(Integer nearbyDriversNotified) { this.nearbyDriversNotified = nearbyDriversNotified; }
}
