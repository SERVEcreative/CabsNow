package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.DTO.DriverLocationRequest;
import com.servecreative.WholeProject.Services.DriverService;
import com.servecreative.WholeProject.Services.RatingService;
import com.servecreative.WholeProject.securityConfig.AuthenticatedUser;
import com.servecreative.WholeProject.securityConfig.SecurityHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;
    private final RatingService ratingService;
    private final SecurityHelper securityHelper;

    public DriverController(DriverService driverService, RatingService ratingService,
                            SecurityHelper securityHelper) {
        this.driverService = driverService;
        this.ratingService = ratingService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/accept/{dutyId}")
    public ResponseEntity<String> acceptDuty(@PathVariable int dutyId) {
        AuthenticatedUser driver = securityHelper.requireDriver();
        return ResponseEntity.ok(driverService.acceptDuty(driver.id(), dutyId));
    }

    @PostMapping("/ignore/{dutyId}")
    public ResponseEntity<String> ignoreDuty(@PathVariable int dutyId) {
        AuthenticatedUser driver = securityHelper.requireDriver();
        return ResponseEntity.ok(driverService.ignoreDuty(driver.id(), dutyId));
    }

    @PostMapping("/complete/{dutyId}")
    public ResponseEntity<String> completeDuty(@PathVariable int dutyId) {
        AuthenticatedUser driver = securityHelper.requireDriver();
        return ResponseEntity.ok(driverService.completeDuty(driver.id(), dutyId));
    }

    @PutMapping("/location")
    public ResponseEntity<Void> updateLocation(@Valid @RequestBody DriverLocationRequest request) {
        AuthenticatedUser driver = securityHelper.requireDriver();
        driverService.updateLocation(driver.id(), request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/online")
    public ResponseEntity<Map<String, Object>> goOnline(@Valid @RequestBody DriverLocationRequest request) {
        AuthenticatedUser driver = securityHelper.requireDriver();
        driverService.goOnline(driver.id(), request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "driverId", driver.id(),
                "message", "You will receive nearby ride requests only"));
    }

    @PostMapping("/offline")
    public ResponseEntity<Map<String, String>> goOffline() {
        AuthenticatedUser driver = securityHelper.requireDriver();
        driverService.goOffline(driver.id());
        return ResponseEntity.ok(Map.of("status", "OFFLINE"));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Map<String, Object>>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm) {
        return ResponseEntity.ok(driverService.findNearbyDrivers(lat, lng, radiusKm));
    }

    @GetMapping("/{driverId}/rating")
    public ResponseEntity<Map<String, Object>> driverRating(@PathVariable int driverId) {
        return ResponseEntity.ok(ratingService.getDriverRatingSummary(driverId));
    }
}
