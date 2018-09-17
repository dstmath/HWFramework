package com.android.server.wm;

public class HwInputMonitor {
    private static final String TAG = "HwInputMonitor";
    private static HwInputMonitor mInstance = null;

    protected HwInputMonitor() {
    }

    public static HwInputMonitor getDefault() {
        if (mInstance == null) {
            mInstance = new HwInputMonitor();
        }
        return mInstance;
    }
}
