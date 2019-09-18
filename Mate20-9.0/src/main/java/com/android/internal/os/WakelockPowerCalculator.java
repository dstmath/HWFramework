package com.android.internal.os;

import android.os.BatteryStats;
import android.util.ArrayMap;

public class WakelockPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "WakelockPowerCalculator";
    private final double mPowerWakelock;
    private long mTotalAppWakelockTimeMs = 0;

    public WakelockPowerCalculator(PowerProfile profile) {
        this.mPowerWakelock = profile.getAveragePower(PowerProfile.POWER_CPU_IDLE);
    }

    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatterySipper batterySipper = app;
        ArrayMap<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats = u.getWakelockStats();
        int wakelockStatsCount = wakelockStats.size();
        long wakeLockTimeUs = 0;
        for (int i = 0; i < wakelockStatsCount; i++) {
            BatteryStats.Timer timer = ((BatteryStats.Uid.Wakelock) wakelockStats.valueAt(i)).getWakeTime(0);
            if (timer != null) {
                wakeLockTimeUs += timer.getTotalTimeLocked(rawRealtimeUs, statsType);
            } else {
                long j = rawRealtimeUs;
                int i2 = statsType;
            }
        }
        long j2 = rawRealtimeUs;
        int i3 = statsType;
        batterySipper.wakeLockTimeMs = wakeLockTimeUs / 1000;
        this.mTotalAppWakelockTimeMs += batterySipper.wakeLockTimeMs;
        batterySipper.wakeLockPowerMah = (((double) batterySipper.wakeLockTimeMs) * this.mPowerWakelock) / 3600000.0d;
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        if (stats != null) {
            long wakeTimeMillis = (stats.getBatteryUptime(rawUptimeUs) / 1000) - (this.mTotalAppWakelockTimeMs + (stats.getScreenOnTime(rawRealtimeUs, statsType) / 1000));
            if (wakeTimeMillis > 0) {
                app.wakeLockTimeMs += wakeTimeMillis;
                app.wakeLockPowerMah += (((double) wakeTimeMillis) * this.mPowerWakelock) / 3600000.0d;
            }
        }
    }

    public void reset() {
        this.mTotalAppWakelockTimeMs = 0;
    }
}
