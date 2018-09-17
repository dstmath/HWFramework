package com.android.internal.os;

import android.os.BatteryStats;
import android.os.BatteryStats.Uid;

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

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        app.wifiRxPackets = u.getNetworkActivityPackets(2, statsType);
        app.wifiTxPackets = u.getNetworkActivityPackets(3, statsType);
        app.wifiRxBytes = u.getNetworkActivityBytes(2, statsType);
        app.wifiTxBytes = u.getNetworkActivityBytes(3, statsType);
        double wifiPacketPower = ((double) (app.wifiRxPackets + app.wifiTxPackets)) * this.mWifiPowerPerPacket;
        app.wifiRunningTimeMs = u.getWifiRunningTime(rawRealtimeUs, statsType) / 1000;
        this.mTotalAppWifiRunningTimeMs += app.wifiRunningTimeMs;
        double wifiLockPower = (((double) app.wifiRunningTimeMs) * this.mWifiPowerOn) / 3600000.0d;
        double wifiScanPower = (((double) (u.getWifiScanTime(rawRealtimeUs, statsType) / 1000)) * this.mWifiPowerScan) / 3600000.0d;
        double wifiBatchScanPower = 0.0d;
        for (int bin = 0; bin < 5; bin++) {
            wifiBatchScanPower += (((double) (u.getWifiBatchedScanTime(bin, rawRealtimeUs, statsType) / 1000)) * this.mWifiPowerBatchScan) / 3600000.0d;
        }
        app.wifiPowerMah = ((wifiPacketPower + wifiLockPower) + wifiScanPower) + wifiBatchScanPower;
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        long totalRunningTimeMs = stats.getGlobalWifiRunningTime(rawRealtimeUs, statsType) / 1000;
        double powerDrain = (((double) (totalRunningTimeMs - this.mTotalAppWifiRunningTimeMs)) * this.mWifiPowerOn) / 3600000.0d;
        app.wifiRunningTimeMs = totalRunningTimeMs;
        app.wifiPowerMah = Math.max(0.0d, powerDrain);
    }

    public void reset() {
        this.mTotalAppWifiRunningTimeMs = 0;
    }
}
