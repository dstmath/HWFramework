package com.android.server.wifi;

import android.util.Log;
import java.util.Iterator;
import java.util.LinkedList;

public class SelfRecovery {
    public static final long MAX_RESTARTS_IN_TIME_WINDOW = 3;
    public static final long MAX_RESTARTS_TIME_WINDOW_MILLIS = 60000;
    public static final int REASON_LAST_RESORT_WATCHDOG = 0;
    public static final int REASON_STA_IFACE_DOWN = 2;
    protected static final String[] REASON_STRINGS = {"Last Resort Watchdog", "WifiNative Failure", "Sta Interface Down"};
    public static final int REASON_WIFINATIVE_FAILURE = 1;
    public static final long RECOVERY_DELAY_TIME_MILLIS = 200;
    private static final String TAG = "WifiSelfRecovery";
    private final Clock mClock;
    private final LinkedList<Long> mPastRestartTimes = new LinkedList<>();
    private final WifiController mWifiController;

    public SelfRecovery(WifiController wifiController, Clock clock) {
        this.mWifiController = wifiController;
        this.mClock = clock;
    }

    public void trigger(int reason) {
        if (reason != 0 && reason != 1 && reason != 2) {
            Log.e(TAG, "Invalid trigger reason. Ignoring...");
        } else if (reason == 2) {
            Log.e(TAG, "STA interface down, disable wifi");
            this.mWifiController.sendMessage(155667);
        } else {
            Log.e(TAG, "Triggering recovery for reason: " + REASON_STRINGS[reason]);
            if (reason == 1) {
                trimPastRestartTimes();
                if (((long) this.mPastRestartTimes.size()) >= 3) {
                    Log.e(TAG, "Already restarted wifi (3) times in last (60000ms ). Disabling wifi");
                    this.mWifiController.sendMessage(155667);
                    return;
                }
                this.mPastRestartTimes.add(Long.valueOf(this.mClock.getElapsedSinceBootMillis()));
            }
            this.mWifiController.sendMessageDelayed(155665, reason, 200);
            Log.e(TAG, "CMD_RECOVERY_RESTART_WIFI, Delay 200");
        }
    }

    private void trimPastRestartTimes() {
        Iterator<Long> iter = this.mPastRestartTimes.iterator();
        long now = this.mClock.getElapsedSinceBootMillis();
        while (iter.hasNext() && now - iter.next().longValue() > 60000) {
            iter.remove();
        }
    }
}
