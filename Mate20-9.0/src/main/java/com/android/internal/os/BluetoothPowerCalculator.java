package com.android.internal.os;

import android.os.BatteryStats;

public class BluetoothPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "BluetoothPowerCalculator";
    private double mAppTotalPowerMah = 0.0d;
    private long mAppTotalTimeMs = 0;
    private final double mIdleMa;
    private final double mRxMa;
    private final double mTxMa;

    public BluetoothPowerCalculator(PowerProfile profile) {
        this.mIdleMa = profile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_IDLE);
        this.mRxMa = profile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_RX);
        this.mTxMa = profile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_TX);
    }

    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatterySipper batterySipper = app;
        BatteryStats.Uid uid = u;
        int i = statsType;
        BatteryStats.ControllerActivityCounter counter = u.getBluetoothControllerActivity();
        if (counter != null) {
            long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(i);
            long rxTimeMs = counter.getRxTimeCounter().getCountLocked(i);
            long txTimeMs = counter.getTxTimeCounters()[0].getCountLocked(i);
            long totalTimeMs = idleTimeMs + txTimeMs + rxTimeMs;
            double powerMah = ((double) counter.getPowerCounter().getCountLocked(i)) / 3600000.0d;
            if (powerMah == 0.0d) {
                double d = powerMah;
                BatteryStats.ControllerActivityCounter controllerActivityCounter = counter;
                long j = idleTimeMs;
                long j2 = rxTimeMs;
                powerMah = (((((double) idleTimeMs) * this.mIdleMa) + (((double) rxTimeMs) * this.mRxMa)) + (((double) txTimeMs) * this.mTxMa)) / 3600000.0d;
            } else {
                long j3 = idleTimeMs;
                long j4 = rxTimeMs;
                double d2 = powerMah;
            }
            batterySipper.bluetoothPowerMah = powerMah;
            batterySipper.bluetoothRunningTimeMs = totalTimeMs;
            batterySipper.btRxBytes = uid.getNetworkActivityBytes(4, i);
            batterySipper.btTxBytes = uid.getNetworkActivityBytes(5, i);
            this.mAppTotalPowerMah += powerMah;
            this.mAppTotalTimeMs += totalTimeMs;
        }
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatterySipper batterySipper = app;
        int i = statsType;
        BatteryStats.ControllerActivityCounter counter = stats.getBluetoothControllerActivity();
        long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(i);
        long txTimeMs = counter.getTxTimeCounters()[0].getCountLocked(i);
        long rxTimeMs = counter.getRxTimeCounter().getCountLocked(i);
        long totalTimeMs = idleTimeMs + txTimeMs + rxTimeMs;
        double powerMah = ((double) counter.getPowerCounter().getCountLocked(i)) / 3600000.0d;
        if (powerMah == 0.0d) {
            BatteryStats.ControllerActivityCounter controllerActivityCounter = counter;
            long j = idleTimeMs;
            powerMah = (((((double) idleTimeMs) * this.mIdleMa) + (((double) rxTimeMs) * this.mRxMa)) + (((double) txTimeMs) * this.mTxMa)) / 3600000.0d;
        } else {
            long j2 = idleTimeMs;
        }
        batterySipper.bluetoothPowerMah = Math.max(0.0d, powerMah - this.mAppTotalPowerMah);
        batterySipper.bluetoothRunningTimeMs = Math.max(0, totalTimeMs - this.mAppTotalTimeMs);
    }

    public void reset() {
        this.mAppTotalPowerMah = 0.0d;
        this.mAppTotalTimeMs = 0;
    }
}
