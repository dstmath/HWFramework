package com.android.internal.os;

import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;

public class FlashlightPowerCalculator extends PowerCalculator {
    private final double mFlashlightPowerOnAvg;

    public FlashlightPowerCalculator(PowerProfile profile) {
        this.mFlashlightPowerOnAvg = profile.getAveragePower(PowerProfile.POWER_FLASHLIGHT);
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        Timer timer = u.getFlashlightTurnedOnTimer();
        if (timer != null) {
            long totalTime = timer.getTotalTimeLocked(rawRealtimeUs, statsType) / 1000;
            app.flashlightTimeMs = totalTime;
            app.flashlightPowerMah = (((double) totalTime) * this.mFlashlightPowerOnAvg) / 3600000.0d;
            return;
        }
        app.flashlightTimeMs = 0;
        app.flashlightPowerMah = 0.0d;
    }
}
