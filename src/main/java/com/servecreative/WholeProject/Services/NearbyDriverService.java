package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Repository.DriverRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NearbyDriverService {

    private final DriverRepository driverRepository;
    private final DriverPresenceService presenceService;
    private final double defaultRadiusKm;

    public NearbyDriverService(
            DriverRepository driverRepository,
            DriverPresenceService presenceService,
            @Value("${app.ride.match-radius-km:10}") double defaultRadiusKm) {
        this.driverRepository = driverRepository;
        this.presenceService = presenceService;
        this.defaultRadiusKm = defaultRadiusKm;
    }

    public List<Integer> findNearbyDrivers(double pickupLat, double pickupLng, Double radiusKm) {
        double radius = radiusKm != null ? radiusKm : defaultRadiusKm;
        List<Integer> onlineIds = presenceService.getOnlineDriverIds();

        List<Driver> candidates = driverRepository.findAvailableWithLocation().stream()
                .filter(d -> d.getStatus() == Driver.DriverStatus.AVAILABLE)
                .filter(d -> onlineIds.isEmpty() || onlineIds.contains(d.getDriverId()))
                .toList();

        List<ScoredDriver> scored = new ArrayList<>();
        for (Driver driver : candidates) {
            if (driver.getLatitude() == null || driver.getLongitude() == null) {
                continue;
            }
            double distance = haversineKm(pickupLat, pickupLng, driver.getLatitude(), driver.getLongitude());
            if (distance <= radius) {
                scored.add(new ScoredDriver(driver.getDriverId(), distance));
            }
        }

        scored.sort(Comparator.comparingDouble(ScoredDriver::distance));

        if (scored.isEmpty()) {
            return candidates.stream()
                    .map(Driver::getDriverId)
                    .limit(20)
                    .toList();
        }

        return scored.stream()
                .map(ScoredDriver::driverId)
                .limit(20)
                .toList();
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

    private record ScoredDriver(int driverId, double distance) {}
}
