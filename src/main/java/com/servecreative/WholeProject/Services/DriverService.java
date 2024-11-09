package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    @Autowired
    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public Optional<Driver> getDriverById(int driverId) {
        return driverRepository.findById(driverId);
    }

    public Driver saveDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public void deleteDriver(int driverId) {
        driverRepository.deleteById(driverId);
    }

    public List<Driver> getDriversByStatus(String status) {
        return driverRepository.findByStatus(status);
    }
}
