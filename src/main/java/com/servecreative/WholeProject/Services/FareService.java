package com.servecreative.WholeProject.Services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FareService {

    private static final String GRAPH_HOPPER_URL = "https://graphhopper.com/api/1/route";

    private static final Map<String, Double> BASE_FARES = new HashMap<>();
    private static final Map<String, Double> PER_KM_RATES = new HashMap<>();

    private static final double SURGE_MULTIPLIER = 1.5;

    private final RestTemplate restTemplate;
    private final String graphHopperApiKey;

    static {
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

    public FareService(RestTemplate restTemplate, @Value("${graphhopper.api.key:}") String graphHopperApiKey) {
        this.restTemplate = restTemplate;
        this.graphHopperApiKey = graphHopperApiKey;
    }

    public double calculateFare(String pickupLat, String pickupLon, String dropLat, String dropLon, String vehicleType) {
        double distance = getDistanceFromGraphHopper(pickupLat, pickupLon, dropLat, dropLon);

        vehicleType = vehicleType.toUpperCase();
        if (!BASE_FARES.containsKey(vehicleType)) {
            vehicleType = "ECONOMY";
        }

        double baseFare = BASE_FARES.get(vehicleType);
        double perKmRate = PER_KM_RATES.get(vehicleType);
        double fare = baseFare + (perKmRate * distance);

        if (isPeakHour()) {
            fare *= SURGE_MULTIPLIER;
        }

        return Math.round(fare * 100.0) / 100.0;
    }

    private double getDistanceFromGraphHopper(String pickupLat, String pickupLon, String dropLat, String dropLon) {
        if (graphHopperApiKey == null || graphHopperApiKey.isBlank()) {
            return estimateFallbackDistance(pickupLat, pickupLon, dropLat, dropLon);
        }

        String url = GRAPH_HOPPER_URL + "?point=" + pickupLat + "," + pickupLon
                + "&point=" + dropLat + "," + dropLon + "&profile=car&key=" + graphHopperApiKey;

        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            return json.getJSONArray("paths")
                    .getJSONObject(0)
                    .getDouble("distance") / 1000.0;
        } catch (Exception e) {
            return estimateFallbackDistance(pickupLat, pickupLon, dropLat, dropLon);
        }
    }

    private double estimateFallbackDistance(String pickupLat, String pickupLon, String dropLat, String dropLon) {
        try {
            double lat1 = Double.parseDouble(pickupLat);
            double lon1 = Double.parseDouble(pickupLon);
            double lat2 = Double.parseDouble(dropLat);
            double lon2 = Double.parseDouble(dropLon);
            return haversineKm(lat1, lon1, lat2, lon2);
        } catch (NumberFormatException e) {
            return 5.0;
        }
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(10, 0)))
                || (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(20, 0)));
    }
}
