package com.android.server.wifi;

import android.net.wifi.WifiInfo;

public class AggressiveConnectedScore extends ConnectedScore {
    private int mFrequencyMHz = ScoringParams.BAND5;
    private int mRssi = 0;
    private final ScoringParams mScoringParams;

    public AggressiveConnectedScore(ScoringParams scoringParams, Clock clock) {
        super(clock);
        this.mScoringParams = scoringParams;
    }

    public void updateUsingRssi(int rssi, long millis, double standardDeviation) {
        this.mRssi = rssi;
    }

    public void updateUsingWifiInfo(WifiInfo wifiInfo, long millis) {
        this.mFrequencyMHz = wifiInfo.getFrequency();
        this.mRssi = wifiInfo.getRssi();
    }

    public void reset() {
        this.mFrequencyMHz = ScoringParams.BAND5;
    }

    public int generateScore() {
        return (this.mRssi - this.mScoringParams.getSufficientRssi(this.mFrequencyMHz)) + 50;
    }
}
