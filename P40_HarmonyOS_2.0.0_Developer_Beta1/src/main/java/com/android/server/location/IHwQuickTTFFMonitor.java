package com.android.server.location;

import android.location.Location;
import android.location.LocationRequest;
import com.android.server.LocationManagerService;

public interface IHwQuickTTFFMonitor {
    boolean checkLocationChanged(Location location, LocationManagerService.LocationProvider locationProvider);

    boolean isLocationReportToApp(String str, String str2, Location location);

    boolean isQuickLocation(Location location);

    void onStartNavigating();

    void onStopNavigating();

    void removeHwQuickTTFF(String str, String str2, boolean z);

    int reportLocationEx(Location location);

    void requestHwQuickTTFF(LocationRequest locationRequest, String str, String str2, String str3);

    void setPermission(boolean z);
}
