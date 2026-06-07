package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.RideStatusEvent;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Repository.DutyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideLocationPublisher {

    private final RideMessageBroadcaster broadcaster;
    private final DutyRepository dutyRepository;

    public RideLocationPublisher(RideMessageBroadcaster broadcaster, DutyRepository dutyRepository) {
        this.broadcaster = broadcaster;
        this.dutyRepository = dutyRepository;
    }

    public void publishDriverLocation(int driverId, double latitude, double longitude) {
        List<Duty> activeDuties = dutyRepository.findDutiesByDriverIdAndStatus(
                driverId, Duty.DutyStatus.ACCEPTED);
        if (activeDuties.isEmpty()) {
            return;
        }

        Duty duty = activeDuties.get(0);
        RideStatusEvent event = buildLocationEvent(duty, driverId, latitude, longitude);

        broadcaster.dispatch("/topic/rider/" + duty.getRider().getRiderId(), event);
        broadcaster.dispatch("/topic/ride/" + duty.getDutyId(), event);
        broadcaster.dispatch("/topic/driver/" + driverId, event);
    }

    private RideStatusEvent buildLocationEvent(Duty duty, int driverId, double latitude, double longitude) {
        RideStatusEvent event = new RideStatusEvent(
                duty.getDutyId(),
                duty.getRider().getRiderId(),
                driverId,
                duty.getStatus().name(),
                "Driver location updated");

        event.setEventType("LOCATION_UPDATE");
        event.setDriverLat(latitude);
        event.setDriverLng(longitude);
        event.setPickupLocation(duty.getPickupLocation());
        event.setDropLocation(duty.getDropLocation());
        event.setPickupLat(duty.getPickupLat());
        event.setPickupLng(duty.getPickupLng());
        event.setDropLat(duty.getDropLat());
        event.setDropLng(duty.getDropLng());
        event.setFare(duty.getFare());
        event.setVehicleType(duty.getVehicleType() != null ? duty.getVehicleType().name() : null);

        if (duty.getAssignedDriver() != null) {
            event.setDriverName(duty.getAssignedDriver().getName());
        }

        return event;
    }
}
