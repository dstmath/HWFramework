package com.android.server.wifi;

import android.net.wifi.WifiInfo;

public class AggressiveConnectedScore extends ConnectedScore {
    private int mFrequencyMHz = 5000;
    private int mRssi = 0;
    private final ScoringParams mScoringParams;

    public AggressiveConnectedScore(ScoringParams scoringParams, Clock clock) {
        super(clock);
        this.mScoringParams = scoringParams;
    }

    @Override // com.android.server.wifi.ConnectedScore
    public void updateUsingRssi(int rssi, long millis, double standardDeviation) {
        this.mRssi = rssi;
    }

    @Override // com.android.server.wifi.ConnectedScore
    public void updateUsingWifiInfo(WifiInfo wifiInfo, long millis) {
        this.mFrequencyMHz = wifiInfo.getFrequency();
        this.mRssi = wifiInfo.getRssi();
    }

    @Override // com.android.server.wifi.ConnectedScore
    public void reset() {
        this.mFrequencyMHz = 5000;
    }

    @Override // com.android.server.wifi.ConnectedScore
    public int generateScore() {
        return (this.mRssi - this.mScoringParams.getSufficientRssi(this.mFrequencyMHz)) + 50;
    }
}
