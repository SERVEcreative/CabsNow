package com.servecreative.WholeProject.Services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryDriverPresenceService implements DriverPresenceService {

    private final Map<Integer, double[]> onlineDrivers = new ConcurrentHashMap<>();

    @Override
    public void markOnline(int driverId, double latitude, double longitude) {
        onlineDrivers.put(driverId, new double[]{latitude, longitude});
    }

    @Override
    public void markOffline(int driverId) {
        onlineDrivers.remove(driverId);
    }

    @Override
    public void updateLocation(int driverId, double latitude, double longitude) {
        if (onlineDrivers.containsKey(driverId)) {
            onlineDrivers.put(driverId, new double[]{latitude, longitude});
        }
    }

    @Override
    public List<Integer> getOnlineDriverIds() {
        return onlineDrivers.keySet().stream().sorted().collect(Collectors.toList());
    }
}
