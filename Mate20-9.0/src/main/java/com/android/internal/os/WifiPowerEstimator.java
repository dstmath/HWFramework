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

    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatterySipper batterySipper = app;
        BatteryStats.Uid uid = u;
        long j = rawRealtimeUs;
        int i = statsType;
        batterySipper.wifiRxPackets = uid.getNetworkActivityPackets(2, i);
        batterySipper.wifiTxPackets = uid.getNetworkActivityPackets(3, i);
        batterySipper.wifiRxBytes = uid.getNetworkActivityBytes(2, i);
        batterySipper.wifiTxBytes = uid.getNetworkActivityBytes(3, i);
        double wifiPacketPower = ((double) (batterySipper.wifiRxPackets + batterySipper.wifiTxPackets)) * this.mWifiPowerPerPacket;
        batterySipper.wifiRunningTimeMs = uid.getWifiRunningTime(j, i) / 1000;
        this.mTotalAppWifiRunningTimeMs += batterySipper.wifiRunningTimeMs;
        double wifiLockPower = (((double) batterySipper.wifiRunningTimeMs) * this.mWifiPowerOn) / 3600000.0d;
        long wifiScanTimeMs = uid.getWifiScanTime(j, i) / 1000;
        long j2 = wifiScanTimeMs;
        double wifiScanPower = (((double) wifiScanTimeMs) * this.mWifiPowerScan) / 3600000.0d;
        double wifiBatchScanPower = 0.0d;
        int bin = 0;
        while (true) {
            int bin2 = bin;
            if (bin2 < 5) {
                long batchScanTimeMs = uid.getWifiBatchedScanTime(bin2, j, i) / 1000;
                long j3 = batchScanTimeMs;
                wifiBatchScanPower += (((double) batchScanTimeMs) * this.mWifiPowerBatchScan) / 3600000.0d;
                bin = bin2 + 1;
                uid = u;
                j = rawRealtimeUs;
                i = statsType;
            } else {
                batterySipper.wifiPowerMah = wifiPacketPower + wifiLockPower + wifiScanPower + wifiBatchScanPower;
                return;
            }
        }
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        long totalRunningTimeMs = stats.getGlobalWifiRunningTime(rawRealtimeUs, statsType) / 1000;
        app.wifiRunningTimeMs = totalRunningTimeMs;
        app.wifiPowerMah = Math.max(0.0d, (((double) (totalRunningTimeMs - this.mTotalAppWifiRunningTimeMs)) * this.mWifiPowerOn) / 3600000.0d);
    }

    public void reset() {
        this.mTotalAppWifiRunningTimeMs = 0;
    }
}
