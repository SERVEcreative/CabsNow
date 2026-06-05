package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Model.Rider;
import com.servecreative.WholeProject.Repository.DriverRepository;
import com.servecreative.WholeProject.Repository.DutyRepository;
import com.servecreative.WholeProject.Repository.RiderRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DutyService {

    private final DutyRepository dutyRepository;
    private final DriverRepository driverRepository;
    private final RiderRepository riderRepository;
    private final RideEventPublisher rideEventPublisher;
    private final EmailNotificationService emailNotificationService;

    public DutyService(
            DutyRepository dutyRepository,
            DriverRepository driverRepository,
            RiderRepository riderRepository,
            RideEventPublisher rideEventPublisher,
            EmailNotificationService emailNotificationService) {
        this.dutyRepository = dutyRepository;
        this.driverRepository = driverRepository;
        this.riderRepository = riderRepository;
        this.rideEventPublisher = rideEventPublisher;
        this.emailNotificationService = emailNotificationService;
    }

    @CacheEvict(value = "pendingDuties", allEntries = true)
    public Duty createDuty(Duty duty, int riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));
        duty.setRider(rider);
        duty.setStatus(Duty.DutyStatus.PENDING);
        Duty saved = dutyRepository.save(duty);
        rideEventPublisher.publish(saved, "New ride request created");
        return saved;
    }

    public Optional<Duty> getDutyById(int dutyId) {
        return dutyRepository.findById(dutyId);
    }

    @Cacheable(value = "pendingDuties")
    public List<Duty> getDutyByStatusPending() {
        return dutyRepository.findByStatus(Duty.DutyStatus.PENDING);
    }

    @CacheEvict(value = "pendingDuties", allEntries = true)
    public Duty rejectDutyForRider(int dutyId, int riderId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Duty not found"));
        if (duty.getRider().getRiderId() != riderId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to reject this duty");
        }
        duty.setStatus(Duty.DutyStatus.REJECTED);
        Duty saved = dutyRepository.save(duty);
        rideEventPublisher.publish(saved, "Ride cancelled by rider");
        return saved;
    }

    @CacheEvict(value = "pendingDuties", allEntries = true)
    public Duty acceptDuty(int dutyId, int driverId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Duty not found"));

        if (duty.getStatus() != Duty.DutyStatus.PENDING) {
            throw new RuntimeException("Ride already taken by another driver");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getStatus() != Driver.DriverStatus.AVAILABLE) {
            throw new RuntimeException("Driver is not available for duty");
        }

        duty.setAssignedDriver(driver);
        duty.setStatus(Duty.DutyStatus.ACCEPTED);
        driver.setStatus(Driver.DriverStatus.ON_DUTY);

        driverRepository.save(driver);
        Duty saved = dutyRepository.save(duty);
        rideEventPublisher.publish(saved, "Driver accepted your ride");
        return saved;
    }

    @CacheEvict(value = "pendingDuties", allEntries = true)
    public Duty completeDuty(int dutyId, int driverId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Duty not found"));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (duty.getAssignedDriver() == null || duty.getAssignedDriver().getDriverId() != driverId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this duty");
        }

        if (duty.getStatus() == Duty.DutyStatus.COMPLETED) {
            throw new RuntimeException("Duty is already completed");
        }

        duty.setStatus(Duty.DutyStatus.COMPLETED);
        driver.setStatus(Driver.DriverStatus.AVAILABLE);

        driverRepository.save(driver);
        Duty saved = dutyRepository.save(duty);
        rideEventPublisher.publish(saved, "Ride completed");
        return saved;
    }

    public List<Duty> getCompletedDutiesByDriver(int driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        return dutyRepository.findByAssignedDriverAndStatus(driver, Duty.DutyStatus.COMPLETED);
    }
}
