package com.android.server.location;

import android.location.Location;
import android.location.LocationRequest;
import com.android.server.LocationManagerService;

public class HwNullQuickTTFFMonitor implements IHwQuickTTFFMonitor {
    public void onStartNavigating() {
    }

    public void onStopNavigating() {
    }

    public int reportLocationEx(Location location) {
        return 0;
    }

    public void setPermission(boolean permission) {
    }

    public void requestHwQuickTTFF(LocationRequest request, String packageName, String requestProvider, String id) {
    }

    public void removeHwQuickTTFF(String packageName, String id, boolean isGps) {
    }

    public boolean isQuickLocation(Location location) {
        return false;
    }

    public boolean checkLocationChanged(Location location, LocationManagerService.LocationProvider provider) {
        return true;
    }

    public boolean isLocationReportToApp(String packageName, String provider, Location location) {
        return true;
    }
}
