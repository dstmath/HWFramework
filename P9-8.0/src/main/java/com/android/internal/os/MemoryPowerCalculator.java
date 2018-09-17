package com.android.internal.os;

import android.os.BatteryStats;
import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;
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
            int i2 = (this.powerAverages[i] > 0.0d ? 1 : (this.powerAverages[i] == 0.0d ? 0 : -1));
        }
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        double totalMah = 0.0d;
        long totalTimeMs = 0;
        LongSparseArray<? extends Timer> timers = stats.getKernelMemoryStats();
        int i = 0;
        while (i < timers.size() && i < this.powerAverages.length) {
            double mAatRail = this.powerAverages[(int) timers.keyAt(i)];
            long timeMs = ((Timer) timers.valueAt(i)).getTotalTimeLocked(rawRealtimeUs, statsType);
            totalMah += ((((double) timeMs) * mAatRail) / 60000.0d) / 60.0d;
            totalTimeMs += timeMs;
            i++;
        }
        app.usagePowerMah = totalMah;
        app.usageTimeMs = totalTimeMs;
    }
}
