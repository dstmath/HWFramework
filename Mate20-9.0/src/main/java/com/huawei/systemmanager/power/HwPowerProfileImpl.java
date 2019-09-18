package com.huawei.systemmanager.power;

import android.os.BatteryStats;
import android.util.ArrayMap;
import com.android.internal.os.PowerProfile;

class HwPowerProfileImpl implements IHwPowerProfile {
    private PowerProfile mPowerProfile;

    public HwPowerProfileImpl(PowerProfile powerprofile) {
        this.mPowerProfile = powerprofile;
    }

    public double getAveragePower(String type, int level) {
        if (this.mPowerProfile != null) {
            return this.mPowerProfile.getAveragePower(type, level);
        }
        return 0.0d;
    }

    public double getAveragePower(String type) {
        if (this.mPowerProfile != null) {
            return this.mPowerProfile.getAveragePowerOrDefault(type, 0.0d);
        }
        return 0.0d;
    }

    public double getBatteryCapacity() {
        if (this.mPowerProfile != null) {
            return this.mPowerProfile.getBatteryCapacity();
        }
        return 0.0d;
    }

    public long getTotalClusterTime(HwBatterySipper sipper) {
        if (this.mPowerProfile == null) {
            return 0;
        }
        BatteryStats.Uid u = sipper.getBatterySipper().uidObj;
        int numClusters = this.mPowerProfile.getNumCpuClusters();
        long totalTime = 0;
        int cluster = 0;
        while (cluster < numClusters) {
            int speedsForCluster = this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
            long totalTime2 = totalTime;
            for (int speed = 0; speed < speedsForCluster; speed++) {
                totalTime2 += u.getTimeAtCpuSpeed(cluster, speed, 2);
            }
            cluster++;
            totalTime = totalTime2;
        }
        return totalTime;
    }

    public double getCpuPowerMaMs(HwBatterySipper sipper, long totalTime) {
        HwPowerProfileImpl hwPowerProfileImpl = this;
        if (hwPowerProfileImpl.mPowerProfile != null) {
            BatteryStats.Uid u = sipper.getBatterySipper().uidObj;
            int i = 2;
            long cpuTimeMs = (u.getUserCpuTimeUs(2) + u.getSystemCpuTimeUs(2)) / 1000;
            int numClusters = hwPowerProfileImpl.mPowerProfile.getNumCpuClusters();
            double cpuPowerMaMs = 0.0d;
            int cluster = 0;
            while (cluster < numClusters) {
                int speedsForCluster = hwPowerProfileImpl.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
                double cpuPowerMaMs2 = cpuPowerMaMs;
                int speed = 0;
                while (speed < speedsForCluster) {
                    double ratio = ((double) u.getTimeAtCpuSpeed(cluster, speed, i)) / ((double) totalTime);
                    PowerProfile powerProfile = hwPowerProfileImpl.mPowerProfile;
                    int speed2 = speed;
                    cpuPowerMaMs2 += ((double) cpuTimeMs) * ratio * powerProfile.getAveragePowerForCpuCore(cluster, speed2);
                    speed = speed2 + 1;
                    hwPowerProfileImpl = this;
                    i = 2;
                }
                long j = totalTime;
                cluster++;
                cpuPowerMaMs = cpuPowerMaMs2;
                hwPowerProfileImpl = this;
                i = 2;
            }
            long j2 = totalTime;
            return cpuPowerMaMs;
        }
        long j3 = totalTime;
        return 0.0d;
    }

    public double getMinAveragePowerForCpu() {
        if (this.mPowerProfile != null) {
            return this.mPowerProfile.getAveragePowerForCpuCore(0, 0);
        }
        return 0.0d;
    }

    public long getCpuFgTimeMs(HwBatterySipper sipper) {
        long cpuFgTimeMs = 0;
        if (this.mPowerProfile != null) {
            ArrayMap<String, ? extends BatteryStats.Uid.Proc> processStats = sipper.getBatterySipper().uidObj.getProcessStats();
            int processStatsCount = processStats.size();
            for (int i = 0; i < processStatsCount; i++) {
                cpuFgTimeMs += ((BatteryStats.Uid.Proc) processStats.valueAt(i)).getForegroundTime(2);
            }
        }
        return cpuFgTimeMs;
    }
}
