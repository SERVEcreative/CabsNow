package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.RideStatusEvent;
import com.servecreative.WholeProject.Model.Duty;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Service
public class RideEventPublisher {

    public static final long ACCEPT_WINDOW_MS = 5000;

    private final RideMessageBroadcaster broadcaster;
    private final NearbyDriverService nearbyDriverService;
    private final DriverPresenceService presenceService;

    public RideEventPublisher(
            RideMessageBroadcaster broadcaster,
            NearbyDriverService nearbyDriverService,
            DriverPresenceService presenceService) {
        this.broadcaster = broadcaster;
        this.nearbyDriverService = nearbyDriverService;
        this.presenceService = presenceService;
    }

    public void publish(Duty duty, String message) {
        publish(duty, message, null, null);
    }

    public void publish(Duty duty, String message, Double pickupLat, Double pickupLng) {
        Integer driverId = duty.getAssignedDriver() != null
                ? duty.getAssignedDriver().getDriverId() : null;

        RideStatusEvent event = new RideStatusEvent(
                duty.getDutyId(),
                duty.getRider().getRiderId(),
                driverId,
                duty.getStatus().name(),
                message);

        event.setPickupLocation(duty.getPickupLocation());
        event.setDropLocation(duty.getDropLocation());
        event.setFare(duty.getFare());
        event.setVehicleType(duty.getVehicleType() != null ? duty.getVehicleType().name() : null);

        if (duty.getAssignedDriver() != null) {
            event.setDriverName(duty.getAssignedDriver().getName());
        }

        if (duty.getStatus() == Duty.DutyStatus.PENDING && duty.getCreatedAt() != null) {
            long createdMs = duty.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            event.setAcceptDeadlineEpochMs(createdMs + ACCEPT_WINDOW_MS);
        }

        dispatch(event, duty, pickupLat, pickupLng);
    }

    private void dispatch(RideStatusEvent event, Duty duty, Double pickupLat, Double pickupLng) {
        java.util.List<Integer> nearbyDrivers = Collections.emptyList();

        if (duty.getStatus() == Duty.DutyStatus.PENDING && pickupLat != null && pickupLng != null) {
            nearbyDrivers = nearbyDriverService.findNearbyDrivers(pickupLat, pickupLng, null);
            event.setNearbyDriversNotified(nearbyDrivers.size());
        }

        broadcaster.dispatch("/topic/ride/" + event.getDutyId(), event);
        broadcaster.dispatch("/topic/rider/" + event.getRiderId(), event);

        if (event.getDriverId() != null) {
            broadcaster.dispatch("/topic/driver/" + event.getDriverId(), event);
        }

        if (duty.getStatus() == Duty.DutyStatus.ACCEPTED) {
            notifyOtherDrivers(event);
            return;
        }

        if (duty.getStatus() == Duty.DutyStatus.PENDING && !nearbyDrivers.isEmpty()) {
            for (int driverId : nearbyDrivers) {
                broadcaster.dispatch("/topic/driver/" + driverId, event);
            }
            return;
        }

        if (duty.getStatus() == Duty.DutyStatus.REJECTED) {
            notifyOnlineDrivers(event);
        }
    }

    private void notifyOnlineDrivers(RideStatusEvent event) {
        for (int driverId : presenceService.getOnlineDriverIds()) {
            broadcaster.dispatch("/topic/driver/" + driverId, event);
        }
    }

    private void notifyOtherDrivers(RideStatusEvent event) {
        for (int driverId : presenceService.getOnlineDriverIds()) {
            if (event.getDriverId() == null || driverId != event.getDriverId()) {
                broadcaster.dispatch("/topic/driver/" + driverId, event);
            }
        }
    }
}
