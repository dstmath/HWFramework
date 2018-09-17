package com.android.internal.os;

import android.os.BatteryStats;
import android.os.BatteryStats.ControllerActivityCounter;
import android.os.BatteryStats.Uid;

public class WifiPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "WifiPowerCalculator";
    private final double mIdleCurrentMa;
    private final double mRxCurrentMa;
    private double mTotalAppPowerDrain = 0.0d;
    private long mTotalAppRunningTime = 0;
    private final double mTxCurrentMa;

    public WifiPowerCalculator(PowerProfile profile) {
        this.mIdleCurrentMa = profile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_IDLE);
        this.mTxCurrentMa = profile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_TX);
        this.mRxCurrentMa = profile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_RX);
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        ControllerActivityCounter counter = u.getWifiControllerActivity();
        if (counter != null) {
            long idleTime = counter.getIdleTimeCounter().getCountLocked(statsType);
            long txTime = counter.getTxTimeCounters()[0].getCountLocked(statsType);
            long rxTime = counter.getRxTimeCounter().getCountLocked(statsType);
            app.wifiRunningTimeMs = (idleTime + rxTime) + txTime;
            this.mTotalAppRunningTime += app.wifiRunningTimeMs;
            app.wifiPowerMah = (((((double) idleTime) * this.mIdleCurrentMa) + (((double) txTime) * this.mTxCurrentMa)) + (((double) rxTime) * this.mRxCurrentMa)) / 3600000.0d;
            this.mTotalAppPowerDrain += app.wifiPowerMah;
            app.wifiRxPackets = u.getNetworkActivityPackets(2, statsType);
            app.wifiTxPackets = u.getNetworkActivityPackets(3, statsType);
            app.wifiRxBytes = u.getNetworkActivityBytes(2, statsType);
            app.wifiTxBytes = u.getNetworkActivityBytes(3, statsType);
        }
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        ControllerActivityCounter counter = stats.getWifiControllerActivity();
        long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(statsType);
        long txTimeMs = counter.getTxTimeCounters()[0].getCountLocked(statsType);
        long rxTimeMs = counter.getRxTimeCounter().getCountLocked(statsType);
        app.wifiRunningTimeMs = Math.max(0, ((idleTimeMs + rxTimeMs) + txTimeMs) - this.mTotalAppRunningTime);
        double powerDrainMah = ((double) counter.getPowerCounter().getCountLocked(statsType)) / 3600000.0d;
        if (powerDrainMah == 0.0d) {
            powerDrainMah = (((((double) idleTimeMs) * this.mIdleCurrentMa) + (((double) txTimeMs) * this.mTxCurrentMa)) + (((double) rxTimeMs) * this.mRxCurrentMa)) / 3600000.0d;
        }
        app.wifiPowerMah = Math.max(0.0d, powerDrainMah - this.mTotalAppPowerDrain);
    }

    public void reset() {
        this.mTotalAppPowerDrain = 0.0d;
        this.mTotalAppRunningTime = 0;
    }
}
