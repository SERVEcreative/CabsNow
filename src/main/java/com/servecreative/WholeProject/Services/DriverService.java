package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Repository.DriverRepository;
import com.servecreative.WholeProject.Repository.DutyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final DutyRepository dutyRepository;
    private final DutyService dutyService;
    private final DriverPresenceService presenceService;

    public DriverService(DriverRepository driverRepository, DutyRepository dutyRepository,
                         DutyService dutyService, DriverPresenceService presenceService) {
        this.driverRepository = driverRepository;
        this.dutyRepository = dutyRepository;
        this.dutyService = dutyService;
        this.presenceService = presenceService;
    }

    @Transactional
    public String acceptDuty(int driverId, int dutyId) {
        dutyService.acceptDuty(dutyId, driverId);
        return "Duty accepted successfully, driver is now ON_DUTY!";
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
        dutyService.completeDuty(dutyId, driverId);
        return "Duty completed successfully, driver is now AVAILABLE!";
    }

    public void updateLocation(int driverId, double latitude, double longitude) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setLatitude(latitude);
        driver.setLongitude(longitude);
        driverRepository.save(driver);
        presenceService.updateLocation(driverId, latitude, longitude);
    }

    public void goOnline(int driverId, double latitude, double longitude) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setLatitude(latitude);
        driver.setLongitude(longitude);
        if (driver.getStatus() == null) {
            driver.setStatus(Driver.DriverStatus.AVAILABLE);
        }
        driverRepository.save(driver);
        presenceService.markOnline(driverId, latitude, longitude);
    }

    public void goOffline(int driverId) {
        presenceService.markOffline(driverId);
    }

    public List<Map<String, Object>> findNearbyDrivers(double lat, double lng, double radiusKm) {
        List<Map<String, Object>> nearby = new ArrayList<>();
        for (Driver driver : driverRepository.findAvailableWithLocation()) {
            double distance = haversineKm(lat, lng, driver.getLatitude(), driver.getLongitude());
            if (distance <= radiusKm) {
                Map<String, Object> info = new HashMap<>();
                info.put("driverId", driver.getDriverId());
                info.put("name", driver.getName());
                info.put("vehicleNumber", driver.getVehicleNumber());
                info.put("latitude", driver.getLatitude());
                info.put("longitude", driver.getLongitude());
                info.put("distanceKm", Math.round(distance * 100.0) / 100.0);
                nearby.add(info);
            }
        }
        nearby.sort(Comparator.comparingDouble(m -> (Double) m.get("distanceKm")));
        return nearby;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
