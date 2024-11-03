package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Repository.DriverRepository;
import com.servecreative.WholeProject.Repository.DutyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DutyService {

    @Autowired
    private DutyRepository dutyRepository;


    @Autowired
    private DriverRepository driverRepository;

    public Duty createDuty(Duty duty) {
        duty.setStatus(Duty.DutyStatus.PENDING); // Default status
        return dutyRepository.save(duty);
    }

    public Optional<Duty> getDutyById(int dutyId) {
        return dutyRepository.findById(dutyId);
    }

    //get All duties with pending status

    public List<Duty> getDutyByStatusPending()
    {
        return dutyRepository.findByStatus(Duty.DutyStatus.PENDING);
    }


    public Duty rejectDuty(int dutyId) {
        Duty duty = dutyRepository.findById(dutyId).orElseThrow(() -> new RuntimeException("Duty not found"));
        duty.setStatus(Duty.DutyStatus.REJECTED);
        return dutyRepository.save(duty);
    }

    public Duty acceptDuty(int dutyId, int driverId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Duty not found"));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Check if driver is available
        if (driver.getStatus() != Driver.DriverStatus.AVAILABLE) {
            throw new RuntimeException("Driver is not available for duty");
        }

        // Update duty and driver status
        duty.setAssignedDriver(driver);
        duty.setStatus(Duty.DutyStatus.ACCEPTED);
        driver.setStatus(Driver.DriverStatus.ON_DUTY);

        // Save updated entities
        driverRepository.save(driver);
        return dutyRepository.save(duty);
    }


    public Duty completeDuty(int dutyId, int driverId) {
        // Find the duty by ID or throw an exception if not found
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Duty not found"));

        // Find the driver by ID or throw an exception if not found
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Check if the duty is already completed to avoid duplicate completion
        if (duty.getStatus() == Duty.DutyStatus.COMPLETED) {
            throw new RuntimeException("Duty is already completed");
        }

        // Update duty status to COMPLETED
        duty.setStatus(Duty.DutyStatus.COMPLETED);

        // Update driver status to AVAILABLE
        driver.setStatus(Driver.DriverStatus.AVAILABLE);

        // Save updated entities
        driverRepository.save(driver);
        return dutyRepository.save(duty);
    }


    public List<Duty> getCompletedDutiesByDriver(int driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        return dutyRepository.findByAssignedDriverAndStatus(driver, Duty.DutyStatus.COMPLETED);
    }


}
