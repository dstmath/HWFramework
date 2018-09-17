package com.android.server.wifi;

import android.util.Log;

public class SelfRecovery {
    public static final int REASON_HAL_CRASH = 1;
    public static final int REASON_LAST_RESORT_WATCHDOG = 0;
    private static final String[] REASON_STRINGS = new String[]{"Last Resort Watchdog", "Hal Crash", "Wificond Crash"};
    public static final int REASON_WIFICOND_CRASH = 2;
    private static final String TAG = "WifiSelfRecovery";
    private final WifiController mWifiController;

    SelfRecovery(WifiController wifiController) {
        this.mWifiController = wifiController;
    }

    public void trigger(int reason) {
        if (reason < 0 || reason > 2) {
            Log.e(TAG, "Invalid trigger reason. Ignoring...");
            return;
        }
        Log.wtf(TAG, "Triggering recovery for reason: " + REASON_STRINGS[reason]);
        this.mWifiController.sendMessage(155665);
    }
}
