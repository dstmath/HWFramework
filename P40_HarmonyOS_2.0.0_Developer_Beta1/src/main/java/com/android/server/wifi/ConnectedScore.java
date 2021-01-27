package com.android.server.wifi;

import android.net.wifi.WifiInfo;

public abstract class ConnectedScore {
    public static final int WIFI_MAX_SCORE = 60;
    public static final int WIFI_MIN_SCORE = 0;
    public static final int WIFI_TRANSITION_SCORE = 50;
    final Clock mClock;
    public double mDefaultRssiStandardDeviation = 2.0d;

    public abstract int generateScore();

    public abstract void reset();

    public abstract void updateUsingRssi(int i, long j, double d);

    public ConnectedScore(Clock clock) {
        this.mClock = clock;
    }

    public long getMillis() {
        return this.mClock.getWallClockMillis();
    }

    public void updateUsingRssi(int rssi, long millis) {
        updateUsingRssi(rssi, millis, this.mDefaultRssiStandardDeviation);
    }

    public void updateUsingWifiInfo(WifiInfo wifiInfo, long millis) {
        updateUsingRssi(wifiInfo.getRssi(), millis);
    }
}
