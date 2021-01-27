package com.huawei.wifi2;

import android.net.wifi.WifiInfo;

public class HwExtendedWifi2Info extends WifiInfo {
    private static final double FILTER_TIME_CONSTANT = 3000.0d;
    private static final double MAX_SAMPLE_WEIGHT = 1.0d;
    private static final double NEGATIVE_VALUE = -1.0d;
    private static final long RESET_TIME_STAMP = Long.MIN_VALUE;
    private static final int RX_GOOD = 3;
    private static final double SECOND_TO_MILLISECOND = 1000.0d;
    private static final int SOURCE_LLSTATS = 2;
    private static final int SOURCE_TRAFFIC_COUNTERS = 1;
    private static final int SOURCE_UNKNOWN = 0;
    private static final int TX_BAD = 2;
    private static final int TX_GOOD = 0;
    private static final int TX_RETRY = 1;
    private boolean mIsConnectedMacRandomizationEnable = false;
    private long mLastPacketCountUpdateTimeStamp = RESET_TIME_STAMP;
    private int mLastSource = 0;

    public void reset() {
        super.reset();
        this.mLastSource = 0;
        this.mLastPacketCountUpdateTimeStamp = RESET_TIME_STAMP;
        if (this.mIsConnectedMacRandomizationEnable) {
            setMacAddress("02:00:00:00:00:00");
        }
    }

    public void updatePacketRates(WifiLinkLayerStats stats, long timeStamp) {
        update(2, new long[]{stats.txmpduBe + stats.txmpduBk + stats.txmpduVi + stats.txmpduVo, stats.retriesBe + stats.retriesBk + stats.retriesVi + stats.retriesVo, stats.lostmpduBe + stats.lostmpduBk + stats.lostmpduVi + stats.lostmpduVo, stats.rxmpduBe + stats.rxmpduBk + stats.rxmpduVi + stats.rxmpduVo}, timeStamp);
    }

    public void updatePacketRates(long txPackets, long rxPackets, long timeStamp) {
        update(1, new long[]{txPackets, 0, 0, rxPackets}, timeStamp);
    }

    private void cleanSpeedStatus() {
        this.txBadRate = 0.0d;
        this.txSuccessRate = 0.0d;
        this.rxSuccessRate = 0.0d;
        this.txRetriesRate = 0.0d;
    }

    private void update(int source, long[] txrxStats, long timeStamp) {
        if (this.mLastSource == 1) {
            long j = this.mLastPacketCountUpdateTimeStamp;
            if (j != RESET_TIME_STAMP && j < timeStamp && this.txBad <= txrxStats[2] && this.txSuccess <= txrxStats[0] && this.rxSuccess <= txrxStats[3] && this.txRetries <= txrxStats[1]) {
                long timeDelta = timeStamp - this.mLastPacketCountUpdateTimeStamp;
                double lastSampleWeight = Math.exp((((double) timeDelta) * NEGATIVE_VALUE) / FILTER_TIME_CONSTANT);
                double currentSampleWeight = MAX_SAMPLE_WEIGHT - lastSampleWeight;
                if (!(timeDelta == 0 || currentSampleWeight == 0.0d)) {
                    this.txBadRate = (this.txBadRate * lastSampleWeight) + (((((double) (txrxStats[2] - this.txBad)) * SECOND_TO_MILLISECOND) / ((double) timeDelta)) * currentSampleWeight);
                    this.txSuccessRate = (this.txSuccessRate * lastSampleWeight) + (((((double) (txrxStats[0] - this.txSuccess)) * SECOND_TO_MILLISECOND) / ((double) timeDelta)) * currentSampleWeight);
                    this.rxSuccessRate = (this.rxSuccessRate * lastSampleWeight) + (((((double) (txrxStats[3] - this.rxSuccess)) * SECOND_TO_MILLISECOND) / ((double) timeDelta)) * currentSampleWeight);
                    this.txRetriesRate = (this.txRetriesRate * lastSampleWeight) + (((((double) (txrxStats[1] - this.txRetries)) * SECOND_TO_MILLISECOND) / ((double) timeDelta)) * currentSampleWeight);
                }
                this.txBad = txrxStats[2];
                this.txSuccess = txrxStats[0];
                this.rxSuccess = txrxStats[3];
                this.txRetries = txrxStats[1];
                this.mLastPacketCountUpdateTimeStamp = timeStamp;
            }
        }
        cleanSpeedStatus();
        this.mLastSource = source;
        this.txBad = txrxStats[2];
        this.txSuccess = txrxStats[0];
        this.rxSuccess = txrxStats[3];
        this.txRetries = txrxStats[1];
        this.mLastPacketCountUpdateTimeStamp = timeStamp;
    }

    public void setEnableConnectedMacRandomization(boolean isConnectedMacRandomizationEnable) {
        this.mIsConnectedMacRandomizationEnable = isConnectedMacRandomizationEnable;
    }
}
