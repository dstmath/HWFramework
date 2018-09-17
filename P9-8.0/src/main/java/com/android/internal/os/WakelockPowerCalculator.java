package com.android.internal.os;

import android.os.BatteryStats;
import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Wakelock;
import android.util.ArrayMap;

public class WakelockPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "WakelockPowerCalculator";
    private final double mPowerWakelock;
    private long mTotalAppWakelockTimeMs = 0;

    public WakelockPowerCalculator(PowerProfile profile) {
        this.mPowerWakelock = profile.getAveragePower(PowerProfile.POWER_CPU_AWAKE);
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        long wakeLockTimeUs = 0;
        ArrayMap<String, ? extends Wakelock> wakelockStats = u.getWakelockStats();
        int wakelockStatsCount = wakelockStats.size();
        for (int i = 0; i < wakelockStatsCount; i++) {
            Timer timer = ((Wakelock) wakelockStats.valueAt(i)).getWakeTime(0);
            if (timer != null) {
                wakeLockTimeUs += timer.getTotalTimeLocked(rawRealtimeUs, statsType);
            }
        }
        app.wakeLockTimeMs = wakeLockTimeUs / 1000;
        this.mTotalAppWakelockTimeMs += app.wakeLockTimeMs;
        app.wakeLockPowerMah = (((double) app.wakeLockTimeMs) * this.mPowerWakelock) / 3600000.0d;
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        if (stats != null) {
            long wakeTimeMillis = (stats.getBatteryUptime(rawUptimeUs) / 1000) - (this.mTotalAppWakelockTimeMs + (stats.getScreenOnTime(rawRealtimeUs, statsType) / 1000));
            if (wakeTimeMillis > 0) {
                double power = (((double) wakeTimeMillis) * this.mPowerWakelock) / 3600000.0d;
                app.wakeLockTimeMs += wakeTimeMillis;
                app.wakeLockPowerMah += power;
            }
        }
    }

    public void reset() {
        this.mTotalAppWakelockTimeMs = 0;
    }
}
