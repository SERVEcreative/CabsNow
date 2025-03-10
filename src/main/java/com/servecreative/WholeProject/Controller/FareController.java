package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.Services.FareService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fare")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @GetMapping("/calculate")
    public double getFare(
            @RequestParam String pickupLat,
            @RequestParam String pickupLon,
            @RequestParam String dropLat,
            @RequestParam String dropLon,
            @RequestParam String vehicleType  // Accept vehicle type as input
    ) {
        return fareService.calculateFare(pickupLat, pickupLon, dropLat, dropLon, vehicleType);
    }
}
