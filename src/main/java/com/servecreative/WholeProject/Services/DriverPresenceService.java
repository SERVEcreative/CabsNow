package com.servecreative.WholeProject.Services;

import java.util.List;

public interface DriverPresenceService {

    void markOnline(int driverId, double latitude, double longitude);

    void markOffline(int driverId);

    void updateLocation(int driverId, double latitude, double longitude);

    List<Integer> getOnlineDriverIds();
}
