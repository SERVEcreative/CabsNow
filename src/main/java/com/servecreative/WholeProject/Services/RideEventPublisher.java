package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.RideDispatchMessage;
import com.servecreative.WholeProject.DTO.RideStatusEvent;
import com.servecreative.WholeProject.Model.Duty;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RideEventPublisher {

    public static final long ACCEPT_WINDOW_MS = 30000;

    private final RideEventBus eventBus;
    private final NearbyDriverService nearbyDriverService;
    private final DriverPresenceService presenceService;

    public RideEventPublisher(
            RideEventBus eventBus,
            NearbyDriverService nearbyDriverService,
            DriverPresenceService presenceService) {
        this.eventBus = eventBus;
        this.nearbyDriverService = nearbyDriverService;
        this.presenceService = presenceService;
    }

    public void publish(Duty duty, String message) {
        publish(duty, message, null, null);
    }

    public void publish(Duty duty, String message, Double pickupLat, Double pickupLng) {
        RideStatusEvent event = buildEvent(duty, message);
        List<String> destinations = resolveDestinations(event, duty, pickupLat, pickupLng);
        RideDispatchMessage dispatchMessage = RideDispatchMessage.of(
                duty.getStatus().name(),
                duty.getDutyId(),
                event,
                destinations);
        eventBus.publish(dispatchMessage);
    }

    private RideStatusEvent buildEvent(Duty duty, String message) {
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
            event.setDriverLat(duty.getAssignedDriver().getLatitude());
            event.setDriverLng(duty.getAssignedDriver().getLongitude());
        }

        event.setPickupLat(duty.getPickupLat());
        event.setPickupLng(duty.getPickupLng());
        event.setDropLat(duty.getDropLat());
        event.setDropLng(duty.getDropLng());
        event.setEventType("STATUS_UPDATE");

        if (duty.getStatus() == Duty.DutyStatus.PENDING && duty.getCreatedAt() != null) {
            long createdMs = duty.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            event.setAcceptDeadlineEpochMs(createdMs + ACCEPT_WINDOW_MS);
        }
        return event;
    }

    private List<String> resolveDestinations(
            RideStatusEvent event,
            Duty duty,
            Double pickupLat,
            Double pickupLng) {

        List<String> destinations = new ArrayList<>();
        destinations.add("/topic/ride/" + event.getDutyId());
        destinations.add("/topic/rider/" + event.getRiderId());

        if (event.getDriverId() != null) {
            destinations.add("/topic/driver/" + event.getDriverId());
        }

        if (duty.getStatus() == Duty.DutyStatus.ACCEPTED) {
            for (int driverId : presenceService.getOnlineDriverIds()) {
                if (event.getDriverId() == null || driverId != event.getDriverId()) {
                    destinations.add("/topic/driver/" + driverId);
                }
            }
            return destinations;
        }

        if (duty.getStatus() == Duty.DutyStatus.PENDING && pickupLat != null && pickupLng != null) {
            List<Integer> nearby = nearbyDriverService.findNearbyDrivers(pickupLat, pickupLng, null);
            event.setNearbyDriversNotified(nearby.size());
            for (int driverId : nearby) {
                destinations.add("/topic/driver/" + driverId);
            }
            return destinations;
        }

        if (duty.getStatus() == Duty.DutyStatus.REJECTED) {
            for (int driverId : presenceService.getOnlineDriverIds()) {
                destinations.add("/topic/driver/" + driverId);
            }
        }

        return destinations;
    }
}
