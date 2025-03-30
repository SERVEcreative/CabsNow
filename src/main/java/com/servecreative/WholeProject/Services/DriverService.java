package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Repository.DriverRepository;
import com.servecreative.WholeProject.Repository.DutyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    @Autowired
    private  DriverRepository driverRepository;


    @Autowired
    private DutyRepository dutyRepository;

    @Transactional  // Ensure both updates happen together
    public String acceptDuty(int driverId, int dutyId) {
        int updatedRows = dutyRepository.acceptDuty(dutyId, driverId);
        if (updatedRows > 0) {
            // Now update the driver status to ON_DUTY
            int driverUpdated = driverRepository.updateDriverToOnduty(driverId);

            if (driverUpdated > 0) {
                return "Duty accepted successfully, driver is now ON_DUTY!";
            } else {
                throw new RuntimeException("Failed to update driver status.");
            }
        }
        return "Duty is no longer available!";
    }

    @Transactional
    public String ignoreDuty(int driverId, int dutyId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Duty not found"));

        if (!duty.getStatus().equals(Duty.DutyStatus.ACCEPTED)) {
            return "Duty is not in an accepted state!";
        }

        int updatedRows = dutyRepository.ignoreDuty(dutyId, driverId);

        if (updatedRows > 0) {
            driverRepository.updateDriverToAvailable(driverId);
            return "Duty ignored, driver is now AVAILABLE!";
        }
        return "Could not ignore the duty!";
    }

    @Transactional
    public String completeDuty(int driverId, int dutyId) {
        int updatedRows = dutyRepository.completeDuty(dutyId, driverId);

        if (updatedRows > 0) {
            driverRepository.updateDriverToAvailable(driverId); // Set driver back to AVAILABLE
            return "Duty completed successfully, driver is now AVAILABLE!";
        }
        return "Could not complete the duty!";

    }

    // Get Available Duties for Driver
    public List<Duty> getAvailableDuties() {
        return dutyRepository.findAvailableDuties();
    }
}
