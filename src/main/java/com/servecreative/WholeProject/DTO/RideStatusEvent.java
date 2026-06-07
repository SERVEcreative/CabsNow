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
    private String eventType;
    private Double pickupLat;
    private Double pickupLng;
    private Double dropLat;
    private Double dropLng;
    private Double driverLat;
    private Double driverLng;
    private Double riderLat;
    private Double riderLng;

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
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }
    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }
    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double dropLat) { this.dropLat = dropLat; }
    public Double getDropLng() { return dropLng; }
    public void setDropLng(Double dropLng) { this.dropLng = dropLng; }
    public Double getDriverLat() { return driverLat; }
    public void setDriverLat(Double driverLat) { this.driverLat = driverLat; }
    public Double getDriverLng() { return driverLng; }
    public void setDriverLng(Double driverLng) { this.driverLng = driverLng; }
    public Double getRiderLat() { return riderLat; }
    public void setRiderLat(Double riderLat) { this.riderLat = riderLat; }
    public Double getRiderLng() { return riderLng; }
    public void setRiderLng(Double riderLng) { this.riderLng = riderLng; }
}
