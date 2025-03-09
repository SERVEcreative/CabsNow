package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Rider;
import com.servecreative.WholeProject.Repository.DutyRepository;
import com.servecreative.WholeProject.Repository.RiderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiderService {

    @Autowired
    private DutyRepository dutyRepository;

    @Autowired
    private RiderRepository riderRepository;

    // Book a ride
    public Duty bookRide(int riderId, String pickupLocation, String dropLocation) {

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));

        Duty existingDuty = dutyRepository.findDutiesByRiderIdAndStatus(riderId, Duty.DutyStatus.PENDING);
        if (existingDuty != null) {
            throw new IllegalStateException("You already have a pending ride. Complete or cancel it before booking a new one.");
        }

        Duty duty = new Duty();
        duty.setStatus(Duty.DutyStatus.PENDING);
        duty.setRider(rider);
        duty.setPickupLocation(pickupLocation); // Set pickup location
        duty.setDropLocation(dropLocation); // Set drop location

        return dutyRepository.save(duty);
    }

    //Cancel a Pending ride
    public Duty cancelRide(int riderId) {
        Rider rider = riderRepository.findById(riderId).orElseThrow(()-> new RuntimeException("Rider not available"));
        Duty existingDuty = dutyRepository.findDutiesByRiderIdAndStatus(riderId, Duty.DutyStatus.PENDING);

        if (existingDuty != null) {
            existingDuty.setStatus(Duty.DutyStatus.REJECTED);
        }
        return dutyRepository.save(existingDuty);
    }
}



