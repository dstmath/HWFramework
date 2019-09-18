package com.android.server.wifi;

import android.net.wifi.WifiInfo;

public class ExtendedWifiInfo extends WifiInfo {
    private static final double FILTER_TIME_CONSTANT = 3000.0d;
    private static final long RESET_TIME_STAMP = Long.MIN_VALUE;
    private static final int SOURCE_LLSTATS = 2;
    private static final int SOURCE_TRAFFIC_COUNTERS = 1;
    private static final int SOURCE_UNKNOWN = 0;
    private boolean mEnableConnectedMacRandomization = false;
    private long mLastPacketCountUpdateTimeStamp = RESET_TIME_STAMP;
    private int mLastSource = 0;

    public void reset() {
        super.reset();
        this.mLastSource = 0;
        this.mLastPacketCountUpdateTimeStamp = RESET_TIME_STAMP;
        if (this.mEnableConnectedMacRandomization) {
            setMacAddress("02:00:00:00:00:00");
        }
    }

    public void updatePacketRates(WifiLinkLayerStats stats, long timeStamp) {
        WifiLinkLayerStats wifiLinkLayerStats = stats;
        update(2, wifiLinkLayerStats.txmpdu_be + wifiLinkLayerStats.txmpdu_bk + wifiLinkLayerStats.txmpdu_vi + wifiLinkLayerStats.txmpdu_vo, wifiLinkLayerStats.retries_be + wifiLinkLayerStats.retries_bk + wifiLinkLayerStats.retries_vi + wifiLinkLayerStats.retries_vo, wifiLinkLayerStats.lostmpdu_be + wifiLinkLayerStats.lostmpdu_bk + wifiLinkLayerStats.lostmpdu_vi + wifiLinkLayerStats.lostmpdu_vo, wifiLinkLayerStats.rxmpdu_be + wifiLinkLayerStats.rxmpdu_bk + wifiLinkLayerStats.rxmpdu_vi + wifiLinkLayerStats.rxmpdu_vo, timeStamp);
    }

    public void updatePacketRates(long txPackets, long rxPackets, long timeStamp) {
        update(1, txPackets, 0, 0, rxPackets, timeStamp);
    }

    private void update(int source, long txgood, long txretries, long txbad, long rxgood, long timeStamp) {
        long j;
        int i = source;
        long j2 = txgood;
        long j3 = txretries;
        long j4 = txbad;
        long j5 = rxgood;
        long j6 = timeStamp;
        if (i != this.mLastSource || this.mLastPacketCountUpdateTimeStamp == RESET_TIME_STAMP || this.mLastPacketCountUpdateTimeStamp >= j6 || this.txBad > j4 || this.txSuccess > j2 || this.rxSuccess > j5 || this.txRetries > j3) {
            j = j3;
            this.txBadRate = 0.0d;
            this.txSuccessRate = 0.0d;
            this.rxSuccessRate = 0.0d;
            this.txRetriesRate = 0.0d;
            this.mLastSource = i;
        } else {
            long timeDelta = j6 - this.mLastPacketCountUpdateTimeStamp;
            double lastSampleWeight = Math.exp((-1.0d * ((double) timeDelta)) / FILTER_TIME_CONSTANT);
            double currentSampleWeight = 1.0d - lastSampleWeight;
            this.txBadRate = (this.txBadRate * lastSampleWeight) + (((((double) (j4 - this.txBad)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
            this.txSuccessRate = (this.txSuccessRate * lastSampleWeight) + (((((double) (j2 - this.txSuccess)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
            this.rxSuccessRate = (this.rxSuccessRate * lastSampleWeight) + (((((double) (rxgood - this.rxSuccess)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
            j = txretries;
            double d = lastSampleWeight;
            this.txRetriesRate = (this.txRetriesRate * lastSampleWeight) + (((((double) (j - this.txRetries)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
        }
        this.txBad = txbad;
        this.txSuccess = j2;
        this.rxSuccess = rxgood;
        this.txRetries = j;
        this.mLastPacketCountUpdateTimeStamp = timeStamp;
    }

    public void setEnableConnectedMacRandomization(boolean enableConnectedMacRandomization) {
        this.mEnableConnectedMacRandomization = enableConnectedMacRandomization;
    }
}
