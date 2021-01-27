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
        update(2, stats.txmpdu_be + stats.txmpdu_bk + stats.txmpdu_vi + stats.txmpdu_vo, stats.retries_be + stats.retries_bk + stats.retries_vi + stats.retries_vo, stats.lostmpdu_be + stats.lostmpdu_bk + stats.lostmpdu_vi + stats.lostmpdu_vo, stats.rxmpdu_be + stats.rxmpdu_bk + stats.rxmpdu_vi + stats.rxmpdu_vo, timeStamp);
    }

    public void updatePacketRates(long txPackets, long rxPackets, long timeStamp) {
        update(1, txPackets, 0, 0, rxPackets, timeStamp);
    }

    private void update(int source, long txgood, long txretries, long txbad, long rxgood, long timeStamp) {
        long j;
        if (source == this.mLastSource) {
            long j2 = this.mLastPacketCountUpdateTimeStamp;
            if (j2 != RESET_TIME_STAMP && j2 < timeStamp && this.txBad <= txbad && this.txSuccess <= txgood && this.rxSuccess <= rxgood && this.txRetries <= txretries) {
                long timeDelta = timeStamp - this.mLastPacketCountUpdateTimeStamp;
                double lastSampleWeight = Math.exp((((double) timeDelta) * -1.0d) / FILTER_TIME_CONSTANT);
                double currentSampleWeight = 1.0d - lastSampleWeight;
                this.txBadRate = (this.txBadRate * lastSampleWeight) + (((((double) (txbad - this.txBad)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
                this.txSuccessRate = (this.txSuccessRate * lastSampleWeight) + (((((double) (txgood - this.txSuccess)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
                this.rxSuccessRate = (this.rxSuccessRate * lastSampleWeight) + (((((double) (rxgood - this.rxSuccess)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
                j = txretries;
                this.txRetriesRate = (this.txRetriesRate * lastSampleWeight) + (((((double) (j - this.txRetries)) * 1000.0d) / ((double) timeDelta)) * currentSampleWeight);
                this.txBad = txbad;
                this.txSuccess = txgood;
                this.rxSuccess = rxgood;
                this.txRetries = j;
                this.mLastPacketCountUpdateTimeStamp = timeStamp;
            }
        }
        j = txretries;
        this.txBadRate = 0.0d;
        this.txSuccessRate = 0.0d;
        this.rxSuccessRate = 0.0d;
        this.txRetriesRate = 0.0d;
        this.mLastSource = source;
        this.txBad = txbad;
        this.txSuccess = txgood;
        this.rxSuccess = rxgood;
        this.txRetries = j;
        this.mLastPacketCountUpdateTimeStamp = timeStamp;
    }

    public void setEnableConnectedMacRandomization(boolean enableConnectedMacRandomization) {
        this.mEnableConnectedMacRandomization = enableConnectedMacRandomization;
    }
}
