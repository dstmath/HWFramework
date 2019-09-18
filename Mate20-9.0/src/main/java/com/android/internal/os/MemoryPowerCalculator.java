package com.android.internal.os;

import android.os.BatteryStats;
import android.util.LongSparseArray;

public class MemoryPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    public static final String TAG = "MemoryPowerCalculator";
    private final double[] powerAverages;

    public MemoryPowerCalculator(PowerProfile profile) {
        int numBuckets = profile.getNumElements(PowerProfile.POWER_MEMORY);
        this.powerAverages = new double[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            this.powerAverages[i] = profile.getAveragePower(PowerProfile.POWER_MEMORY, i);
            double d = this.powerAverages[i];
        }
    }

    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatterySipper batterySipper = app;
        double totalMah = 0.0d;
        long totalTimeMs = 0;
        LongSparseArray<? extends BatteryStats.Timer> timers = stats.getKernelMemoryStats();
        int i = 0;
        while (i < timers.size() && i < this.powerAverages.length) {
            double mAatRail = this.powerAverages[(int) timers.keyAt(i)];
            long timeMs = ((BatteryStats.Timer) timers.valueAt(i)).getTotalTimeLocked(rawRealtimeUs, statsType);
            totalMah += ((((double) timeMs) * mAatRail) / 60000.0d) / 60.0d;
            totalTimeMs += timeMs;
            i++;
        }
        int i2 = statsType;
        batterySipper.usagePowerMah = totalMah;
        batterySipper.usageTimeMs = totalTimeMs;
    }
}
