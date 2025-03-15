package com.servecreative.WholeProject.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FareService {

    private static final String GRAPH_HOPPER_API_KEY = "5c0075d2-fe50-4e7e-abb0-1d47cb83e068";  // Replace with your key
    private static final String GRAPH_HOPPER_URL = "https://graphhopper.com/api/1/route";


    // Vehicle-based pricing
    private static final Map<String, Double> BASE_FARES = new HashMap<>();
    private static final Map<String, Double> PER_KM_RATES = new HashMap<>();

    private static final double SURGE_MULTIPLIER = 1.5; // Surge pricing factor

    static {
        // Initialize fares for different vehicle types
        BASE_FARES.put("BIKE", 30.0);
        BASE_FARES.put("ECONOMY", 40.0);
        BASE_FARES.put("SEDAN", 50.0);
        BASE_FARES.put("SUV", 70.0);
        BASE_FARES.put("LUXURY", 100.0);

        PER_KM_RATES.put("BIKE", 5.0);
        PER_KM_RATES.put("ECONOMY", 7.0);
        PER_KM_RATES.put("SEDAN", 10.0);
        PER_KM_RATES.put("SUV", 15.0);
        PER_KM_RATES.put("LUXURY", 25.0);
    }

    public double calculateFare(String pickupLat, String pickupLon, String dropLat, String dropLon, String vehicleType) {
        double distance = getDistanceFromGraphHopper(pickupLat, pickupLon, dropLat, dropLon);

        // Convert vehicle type to uppercase to match map keys
        vehicleType = vehicleType.toUpperCase();

        // Default to ECONOMY if vehicle type is invalid
        if (!BASE_FARES.containsKey(vehicleType)) {
            vehicleType = "ECONOMY";
        }

        double baseFare = BASE_FARES.get(vehicleType);
        double perKmRate = PER_KM_RATES.get(vehicleType);
        double fare = baseFare + (perKmRate * distance);

        // Apply surge pricing if it's peak hours
        if (isPeakHour()) {
            fare *= SURGE_MULTIPLIER;
        }

        return fare;
    }

    private double getDistanceFromGraphHopper(String pickupLat, String pickupLon, String dropLat, String dropLon) {
        String url = GRAPH_HOPPER_URL + "?point=" + pickupLat + "," + pickupLon +
                "&point=" + dropLat + "," + dropLon + "&profile=car&key=" + GRAPH_HOPPER_API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        try {
            JSONObject json = new JSONObject(response);
            return json.getJSONArray("paths")
                    .getJSONObject(0)
                    .getDouble("distance") / 1000.0; // Convert meters to km
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(10, 0))) ||
                (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(20, 0)));
    }
}
