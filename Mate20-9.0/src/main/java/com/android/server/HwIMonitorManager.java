package com.android.server;

import android.util.IMonitor;

public class HwIMonitorManager {
    public static final short FAIL_REASON_VARCHAR = 0;
    public static final String TAG = "HwIMonitorManager";
    private static HwIMonitorManager mHwIMonitorManager = null;

    public static synchronized HwIMonitorManager getInstance() {
        HwIMonitorManager hwIMonitorManager;
        synchronized (HwIMonitorManager.class) {
            if (mHwIMonitorManager == null) {
                mHwIMonitorManager = new HwIMonitorManager();
            }
            hwIMonitorManager = mHwIMonitorManager;
        }
        return hwIMonitorManager;
    }

    public boolean uploadBtRadarEvent(int event, String exception) {
        if (exception == null) {
            return false;
        }
        IMonitor.EventStream eStream = IMonitor.openEventStream(event);
        if (eStream != null) {
            eStream.setParam(0, exception);
        }
        return uploadIMonitorEvent(eStream);
    }

    private boolean uploadIMonitorEvent(IMonitor.EventStream eStream) {
        boolean ret;
        if (eStream == null) {
            HwLog.d(TAG, "eStream is null!");
            return false;
        }
        synchronized (this) {
            ret = IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
        }
        return ret;
    }
}
