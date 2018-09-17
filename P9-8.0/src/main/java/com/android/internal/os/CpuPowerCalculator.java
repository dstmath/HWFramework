package com.android.internal.os;

import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Proc;
import android.util.ArrayMap;
import com.android.internal.telephony.PhoneConstants;

public class CpuPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "CpuPowerCalculator";
    private final PowerProfile mProfile;

    public CpuPowerCalculator(PowerProfile profile) {
        this.mProfile = profile;
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        int cluster;
        int speed;
        app.cpuTimeMs = (u.getUserCpuTimeUs(statsType) + u.getSystemCpuTimeUs(statsType)) / 1000;
        long totalTime = 0;
        int numClusters = this.mProfile.getNumCpuClusters();
        for (cluster = 0; cluster < numClusters; cluster++) {
            for (speed = 0; speed < this.mProfile.getNumSpeedStepsInCpuCluster(cluster); speed++) {
                totalTime += u.getTimeAtCpuSpeed(cluster, speed, statsType);
            }
        }
        totalTime = Math.max(totalTime, 1);
        double cpuPowerMaMs = 0.0d;
        for (cluster = 0; cluster < numClusters; cluster++) {
            for (speed = 0; speed < this.mProfile.getNumSpeedStepsInCpuCluster(cluster); speed++) {
                cpuPowerMaMs += (((double) app.cpuTimeMs) * (((double) u.getTimeAtCpuSpeed(cluster, speed, statsType)) / ((double) totalTime))) * this.mProfile.getAveragePowerForCpu(cluster, speed);
            }
        }
        app.cpuPowerMah = cpuPowerMaMs / 3600000.0d;
        double highestDrain = 0.0d;
        app.cpuFgTimeMs = 0;
        ArrayMap<String, ? extends Proc> processStats = u.getProcessStats();
        int processStatsCount = processStats.size();
        for (int i = 0; i < processStatsCount; i++) {
            Proc ps = (Proc) processStats.valueAt(i);
            String processName = (String) processStats.keyAt(i);
            app.cpuFgTimeMs += ps.getForegroundTime(statsType);
            long costValue = (ps.getUserTime(statsType) + ps.getSystemTime(statsType)) + ps.getForegroundTime(statsType);
            if (app.packageWithHighestDrain != null) {
                if (!app.packageWithHighestDrain.startsWith(PhoneConstants.APN_TYPE_ALL)) {
                    if (highestDrain < ((double) costValue) && (processName.startsWith(PhoneConstants.APN_TYPE_ALL) ^ 1) != 0) {
                        highestDrain = (double) costValue;
                        app.packageWithHighestDrain = processName;
                    }
                }
            }
            highestDrain = (double) costValue;
            app.packageWithHighestDrain = processName;
        }
        if (app.cpuFgTimeMs > app.cpuTimeMs) {
            app.cpuTimeMs = app.cpuFgTimeMs;
        }
    }
}
