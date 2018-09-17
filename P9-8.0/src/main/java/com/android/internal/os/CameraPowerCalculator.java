package com.android.internal.os;

import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;

public class CameraPowerCalculator extends PowerCalculator {
    private final double mCameraPowerOnAvg;

    public CameraPowerCalculator(PowerProfile profile) {
        this.mCameraPowerOnAvg = profile.getAveragePower(PowerProfile.POWER_CAMERA);
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        Timer timer = u.getCameraTurnedOnTimer();
        if (timer != null) {
            long totalTime = timer.getTotalTimeLocked(rawRealtimeUs, statsType) / 1000;
            app.cameraTimeMs = totalTime;
            app.cameraPowerMah = (((double) totalTime) * this.mCameraPowerOnAvg) / 3600000.0d;
            return;
        }
        app.cameraTimeMs = 0;
        app.cameraPowerMah = 0.0d;
    }
}
