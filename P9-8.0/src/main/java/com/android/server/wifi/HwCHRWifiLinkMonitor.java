package com.android.server.wifi;

import android.util.Log;

class HwCHRWifiLinkMonitor {
    private static final String DEBUGWL_FILENAME = "/sys/kernel/debug/bcmdhd/debug_wl_counters";
    private static final String TAG = "HwCHRWifiLinkMonitor";
    private static HwCHRWifiLinkMonitor monitor = null;
    private HwCHRWifiBcmIncrCounterLst bcmLst;

    public HwCHRWifiLinkMonitor() {
        this.bcmLst = null;
        this.bcmLst = new HwCHRWifiBcmIncrCounterLst();
    }

    public static synchronized HwCHRWifiLinkMonitor getDefault() {
        HwCHRWifiLinkMonitor hwCHRWifiLinkMonitor;
        synchronized (HwCHRWifiLinkMonitor.class) {
            if (monitor == null) {
                monitor = new HwCHRWifiLinkMonitor();
            }
            hwCHRWifiLinkMonitor = monitor;
        }
        return hwCHRWifiLinkMonitor;
    }

    public void runCounters() {
        HwCHRWifiBCMCounterReader reader = new HwCHRWifiBCMCounterReader();
        reader.parseValue(HwCHRWifiFile.getFileResult(DEBUGWL_FILENAME));
        this.bcmLst.updateIncrCounters(reader);
        Log.e(TAG, this.bcmLst.toString());
    }

    public HwCHRWifiBcmIncrCounterLst getCounterLst() {
        return this.bcmLst;
    }

    public void setCounterLst(HwCHRWifiBcmIncrCounterLst src) {
        this.bcmLst = src;
    }
}
