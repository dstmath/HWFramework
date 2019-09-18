package com.huawei.hsm.permission;

import android.content.Context;
import android.location.Location;
import android.os.Binder;
import com.huawei.hsm.permission.monitor.PermRecordHandler;

public class LocationPermission {
    private static final String TAG = "LocationPermission";

    public LocationPermission(Context context) {
    }

    private void recordPermissionUsed(int uid, int pid) {
        PermRecordHandler mPermRecHandler = PermRecordHandler.getHandleInstance();
        if (mPermRecHandler != null) {
            mPermRecHandler.accessPermission(uid, pid, 8, null);
        }
    }

    public boolean isLocationBlocked() {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        boolean z = false;
        if (!StubController.checkPrecondition(uid)) {
            recordPermissionUsed(uid, pid);
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
