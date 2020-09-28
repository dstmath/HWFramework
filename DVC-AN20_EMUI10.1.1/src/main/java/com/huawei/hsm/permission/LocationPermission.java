package com.huawei.hsm.permission;

import android.content.Context;
import android.location.Location;

public class LocationPermission {
    private static final String TAG = "LocationPermission";

    public LocationPermission(Context context) {
    }

    public boolean isLocationBlocked() {
        return false;
    }

    public static Location getFakeLocation(String name) {
        Location loc = new Location(name);
        loc.setLongitude(0.0d);
        loc.setLatitude(0.0d);
        return loc;
    }
}
