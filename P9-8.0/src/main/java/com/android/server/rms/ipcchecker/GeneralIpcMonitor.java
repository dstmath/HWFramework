package com.android.server.rms.ipcchecker;

import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.record.ResourceUtils;

public class GeneralIpcMonitor extends HwIpcMonitorImpl {
    private static final String TAG = "RMS.GeneralIpcMonitor";
    private static GeneralIpcMonitor mGeneralIpcMonitor;

    private GeneralIpcMonitor() {
    }

    public static synchronized GeneralIpcMonitor getInstance() {
        GeneralIpcMonitor generalIpcMonitor;
        synchronized (GeneralIpcMonitor.class) {
            if (mGeneralIpcMonitor == null) {
                mGeneralIpcMonitor = new GeneralIpcMonitor();
            }
            generalIpcMonitor = mGeneralIpcMonitor;
        }
        return generalIpcMonitor;
    }

    public boolean action(Object lock) {
        if (Utils.DEBUG) {
            Log.d(TAG, " Do action for GeneralIpcMonitor" + this.mName);
        }
        if (ResourceUtils.killApplicationProcess(ResourceUtils.getLockOwnerPid(lock))) {
            return super.action();
        }
        return false;
    }
}
