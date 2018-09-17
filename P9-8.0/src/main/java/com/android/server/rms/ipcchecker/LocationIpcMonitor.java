package com.android.server.rms.ipcchecker;

import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.record.ResourceUtils;

public class LocationIpcMonitor extends HwIpcMonitorImpl {
    private static final String TAG = "RMS.LocationIpcMonitor";

    private LocationIpcMonitor(Object object, String name) {
        super(object, name);
    }

    public static LocationIpcMonitor getInstance(Object object, String name) {
        return new LocationIpcMonitor(object, name);
    }

    public boolean action() {
        if (Utils.DEBUG) {
            Log.d(TAG, " Do action for this monitor" + this.mName);
        }
        if (ResourceUtils.killApplicationProcess(ResourceUtils.getLockOwnerPid(this.mLock))) {
            return super.action();
        }
        return false;
    }
}
