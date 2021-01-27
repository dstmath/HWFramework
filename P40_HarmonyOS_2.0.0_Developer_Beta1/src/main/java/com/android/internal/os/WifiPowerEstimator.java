package com.android.internal.os;

import android.os.BatteryStats;

public class WifiPowerEstimator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "WifiPowerEstimator";
    private long mTotalAppWifiRunningTimeMs = 0;
    private final double mWifiPowerBatchScan;
    private final double mWifiPowerOn;
    private final double mWifiPowerPerPacket;
    private final double mWifiPowerScan;

    public WifiPowerEstimator(PowerProfile profile) {
        this.mWifiPowerPerPacket = getWifiPowerPerPacket(profile);
        this.mWifiPowerOn = profile.getAveragePower(PowerProfile.POWER_WIFI_ON);
        this.mWifiPowerScan = profile.getAveragePower(PowerProfile.POWER_WIFI_SCAN);
        this.mWifiPowerBatchScan = profile.getAveragePower(PowerProfile.POWER_WIFI_BATCHED_SCAN);
    }

    private static double getWifiPowerPerPacket(PowerProfile profile) {
        return (profile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600.0d) / 61.03515625d;
    }

    @Override // com.android.internal.os.PowerCalculator
    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatteryStats.Uid uid = u;
        long j = rawRealtimeUs;
        int i = statsType;
        app.wifiRxPackets = uid.getNetworkActivityPackets(2, i);
        app.wifiTxPackets = uid.getNetworkActivityPackets(3, i);
        app.wifiRxBytes = uid.getNetworkActivityBytes(2, i);
        app.wifiTxBytes = uid.getNetworkActivityBytes(3, i);
        double wifiPacketPower = ((double) (app.wifiRxPackets + app.wifiTxPackets)) * this.mWifiPowerPerPacket;
        app.wifiRunningTimeMs = uid.getWifiRunningTime(j, i) / 1000;
        this.mTotalAppWifiRunningTimeMs += app.wifiRunningTimeMs;
        double wifiLockPower = (((double) app.wifiRunningTimeMs) * this.mWifiPowerOn) / 3600000.0d;
        double wifiScanPower = (((double) (uid.getWifiScanTime(j, i) / 1000)) * this.mWifiPowerScan) / 3600000.0d;
        int bin = 0;
        double wifiBatchScanPower = 0.0d;
        while (bin < 5) {
            wifiBatchScanPower += (((double) (uid.getWifiBatchedScanTime(bin, j, i) / 1000)) * this.mWifiPowerBatchScan) / 3600000.0d;
            bin++;
            uid = u;
            j = rawRealtimeUs;
            i = statsType;
        }
        app.wifiPowerMah = wifiPacketPower + wifiLockPower + wifiScanPower + wifiBatchScanPower;
    }

    @Override // com.android.internal.os.PowerCalculator
    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        long totalRunningTimeMs = stats.getGlobalWifiRunningTime(rawRealtimeUs, statsType) / 1000;
        app.wifiRunningTimeMs = totalRunningTimeMs;
        app.wifiPowerMah = Math.max(0.0d, (((double) (totalRunningTimeMs - this.mTotalAppWifiRunningTimeMs)) * this.mWifiPowerOn) / 3600000.0d);
    }

    @Override // com.android.internal.os.PowerCalculator
    public void reset() {
        this.mTotalAppWifiRunningTimeMs = 0;
    }
}
