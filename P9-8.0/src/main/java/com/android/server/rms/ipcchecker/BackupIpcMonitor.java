package com.android.server.rms.ipcchecker;

import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.record.ResourceUtils;

public class BackupIpcMonitor extends HwIpcMonitorImpl {
    private static final String TAG = "RMS.BackupIpcMonitor";

    private BackupIpcMonitor(Object object, String name) {
        super(object, name);
    }

    public static BackupIpcMonitor getInstance(Object object, String name) {
        return new BackupIpcMonitor(object, name);
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
