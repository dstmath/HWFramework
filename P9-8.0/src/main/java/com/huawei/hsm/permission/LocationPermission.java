package com.huawei.hsm.permission;

import android.content.Context;
import android.location.Location;
import android.os.Binder;

public class LocationPermission {
    private static final String TAG = "LocationPermission";

    public LocationPermission(Context context) {
    }

    public boolean isLocationBlocked() {
        boolean z = false;
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (!StubController.checkPrecondition(uid)) {
            return false;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(8, uid, pid, null);
        if (selectionResult == 0) {
            return false;
        }
        if (2 == selectionResult) {
            z = true;
        }
        return z;
    }

    public static Location getFakeLocation(String name) {
        Location loc = new Location(name);
        loc.setLongitude(0.0d);
        loc.setLatitude(0.0d);
        return loc;
    }
}
