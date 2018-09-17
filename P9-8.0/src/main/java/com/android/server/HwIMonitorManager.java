package com.android.server;

import android.util.IMonitor;
import android.util.IMonitor.EventStream;

public class HwIMonitorManager {
    public static final short FAIL_REASON_VARCHAR = (short) 0;
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
        EventStream eStream = IMonitor.openEventStream(event);
        if (eStream != null) {
            eStream.setParam((short) 0, exception);
        }
        return uploadIMonitorEvent(eStream);
    }

    private boolean uploadIMonitorEvent(EventStream eStream) {
        if (eStream == null) {
            HwLog.d(TAG, "eStream is null!");
            return false;
        }
        boolean ret;
        synchronized (this) {
            ret = IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
        }
        return ret;
    }
}
