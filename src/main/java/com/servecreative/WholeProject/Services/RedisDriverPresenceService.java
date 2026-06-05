package com.servecreative.WholeProject.Services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisDriverPresenceService implements DriverPresenceService {

    private static final String ONLINE_KEY = "cabsnow:drivers:online";
    private static final String GEO_KEY = "cabsnow:drivers:geo";

    private final StringRedisTemplate redis;

    public RedisDriverPresenceService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void markOnline(int driverId, double latitude, double longitude) {
        redis.opsForSet().add(ONLINE_KEY, String.valueOf(driverId));
        redis.opsForGeo().add(GEO_KEY, new org.springframework.data.geo.Point(longitude, latitude),
                String.valueOf(driverId));
    }

    @Override
    public void markOffline(int driverId) {
        String id = String.valueOf(driverId);
        redis.opsForSet().remove(ONLINE_KEY, id);
        redis.opsForGeo().remove(GEO_KEY, id);
    }

    @Override
    public void updateLocation(int driverId, double latitude, double longitude) {
        if (Boolean.TRUE.equals(redis.opsForSet().isMember(ONLINE_KEY, String.valueOf(driverId)))) {
            markOnline(driverId, latitude, longitude);
        }
    }

    @Override
    public List<Integer> getOnlineDriverIds() {
        Set<String> members = redis.opsForSet().members(ONLINE_KEY);
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = new ArrayList<>();
        for (String member : members) {
            ids.add(Integer.parseInt(member));
        }
        ids.sort(Integer::compareTo);
        return ids;
    }
}
