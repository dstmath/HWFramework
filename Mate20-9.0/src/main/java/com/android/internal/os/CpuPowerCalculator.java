package com.android.internal.os;

import android.os.BatteryStats;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;

public class CpuPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final long MICROSEC_IN_HR = 3600000000L;
    private static final String TAG = "CpuPowerCalculator";
    private final PowerProfile mProfile;

    public CpuPowerCalculator(PowerProfile profile) {
        this.mProfile = profile;
    }

    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        double highestDrain;
        BatterySipper batterySipper = app;
        BatteryStats.Uid uid = u;
        int i = statsType;
        batterySipper.cpuTimeMs = (uid.getUserCpuTimeUs(i) + uid.getSystemCpuTimeUs(i)) / 1000;
        int numClusters = this.mProfile.getNumCpuClusters();
        double cpuPowerMaUs = 0.0d;
        int cluster = 0;
        while (cluster < numClusters) {
            int speedsForCluster = this.mProfile.getNumSpeedStepsInCpuCluster(cluster);
            double cpuPowerMaUs2 = cpuPowerMaUs;
            for (int speed = 0; speed < speedsForCluster; speed++) {
                cpuPowerMaUs2 += ((double) uid.getTimeAtCpuSpeed(cluster, speed, i)) * this.mProfile.getAveragePowerForCpuCore(cluster, speed);
            }
            cluster++;
            cpuPowerMaUs = cpuPowerMaUs2;
        }
        double cpuPowerMaUs3 = cpuPowerMaUs + (((double) (u.getCpuActiveTime() * 1000)) * this.mProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE));
        long[] cpuClusterTimes = u.getCpuClusterTimes();
        if (cpuClusterTimes != null) {
            if (cpuClusterTimes.length == numClusters) {
                for (int i2 = 0; i2 < numClusters; i2++) {
                    cpuPowerMaUs3 += ((double) (cpuClusterTimes[i2] * 1000)) * this.mProfile.getAveragePowerForCpuCluster(i2);
                }
            } else {
                Log.w(TAG, "UID " + u.getUid() + " CPU cluster # mismatch: Power Profile # " + numClusters + " actual # " + cpuClusterTimes.length);
            }
        }
        batterySipper.cpuPowerMah = cpuPowerMaUs3 / 3.6E9d;
        double highestDrain2 = 0.0d;
        batterySipper.cpuFgTimeMs = 0;
        ArrayMap<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
        int processStatsCount = processStats.size();
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 >= processStatsCount) {
                break;
            }
            BatteryStats.Uid.Proc ps = (BatteryStats.Uid.Proc) processStats.valueAt(i4);
            String processName = processStats.keyAt(i4);
            int numClusters2 = numClusters;
            long[] cpuClusterTimes2 = cpuClusterTimes;
            batterySipper.cpuFgTimeMs += ps.getForegroundTime(i);
            long costValue = ps.getUserTime(i) + ps.getSystemTime(i) + ps.getForegroundTime(i);
            if (batterySipper.packageWithHighestDrain == null || batterySipper.packageWithHighestDrain.startsWith(PhoneConstants.APN_TYPE_ALL)) {
                highestDrain = (double) costValue;
                batterySipper.packageWithHighestDrain = processName;
            } else {
                if (highestDrain2 < ((double) costValue) && !processName.startsWith(PhoneConstants.APN_TYPE_ALL)) {
                    highestDrain = (double) costValue;
                    batterySipper.packageWithHighestDrain = processName;
                }
                i3 = i4 + 1;
                numClusters = numClusters2;
                cpuClusterTimes = cpuClusterTimes2;
                BatteryStats.Uid uid2 = u;
                i = statsType;
            }
            highestDrain2 = highestDrain;
            i3 = i4 + 1;
            numClusters = numClusters2;
            cpuClusterTimes = cpuClusterTimes2;
            BatteryStats.Uid uid22 = u;
            i = statsType;
        }
        long[] jArr = cpuClusterTimes;
        if (batterySipper.cpuFgTimeMs > batterySipper.cpuTimeMs) {
            batterySipper.cpuTimeMs = batterySipper.cpuFgTimeMs;
        }
    }
}
