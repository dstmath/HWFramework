package com.huawei.wifi2;

import android.util.SparseArray;

public class WifiLinkLayerStats {
    public static final String V1_0 = "V1_0";
    public static final String V1_3 = "V1_3";
    protected int beaconRx;
    protected final SparseArray<ChannelStats> channelStatsMap = new SparseArray<>();
    protected long lostmpduBe;
    protected long lostmpduBk;
    protected long lostmpduVi;
    protected long lostmpduVo;
    protected int onTime;
    protected int onTimeBackgroundScan = -1;
    protected int onTimeHs20Scan = -1;
    protected int onTimeNanScan = -1;
    protected int onTimePnoScan = -1;
    protected int onTimeRoamScan = -1;
    protected int onTimeScan;
    protected long retriesBe;
    protected long retriesBk;
    protected long retriesVi;
    protected long retriesVo;
    protected int rssiMgmt;
    protected int rxTime;
    protected long rxmpduBe;
    protected long rxmpduBk;
    protected long rxmpduVi;
    protected long rxmpduVo;
    protected long timeStampInMs;
    protected int txTime;
    protected int[] txTimePerLevel;
    protected long txmpduBe;
    protected long txmpduBk;
    protected long txmpduVi;
    protected long txmpduVo;
    protected String version;

    public static class ChannelStats {
        protected int ccaBusyTimeMs;
        protected int frequency;
        protected int radioOnTimeMs;
    }

    public String toString() {
        return " WifiLinkLayerStats: " + System.lineSeparator() + " BE :  rx = " + Long.toString(this.rxmpduBe) + " tx = " + Long.toString(this.txmpduBe) + " lost = " + Long.toString(this.lostmpduBe) + " retries = " + Long.toString(this.retriesBe) + System.lineSeparator();
    }
}
