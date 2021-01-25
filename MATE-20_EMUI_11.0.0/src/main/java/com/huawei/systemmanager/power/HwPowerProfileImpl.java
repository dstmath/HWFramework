package com.huawei.systemmanager.power;

import android.os.BatteryStats;
import android.util.ArrayMap;
import com.android.internal.os.PowerProfile;

class HwPowerProfileImpl implements IHwPowerProfile {
    private PowerProfile mPowerProfile;

    public HwPowerProfileImpl(PowerProfile powerprofile) {
        this.mPowerProfile = powerprofile;
    }

    @Override // com.huawei.systemmanager.power.IHwPowerProfile
    public double getAveragePower(String type, int level) {
        PowerProfile powerProfile = this.mPowerProfile;
        if (powerProfile != null) {
            return powerProfile.getAveragePower(type, level);
        }
        return 0.0d;
    }

    @Override // com.huawei.systemmanager.power.IHwPowerProfile
    public double getAveragePower(String type) {
        PowerProfile powerProfile = this.mPowerProfile;
        if (powerProfile != null) {
            return powerProfile.getAveragePowerOrDefault(type, 0.0d);
        }
        return 0.0d;
    }

    @Override // com.huawei.systemmanager.power.IHwPowerProfile
    public double getBatteryCapacity() {
        PowerProfile powerProfile = this.mPowerProfile;
        if (powerProfile != null) {
            return powerProfile.getBatteryCapacity();
        }
        return 0.0d;
    }

    @Override // com.huawei.systemmanager.power.IHwPowerProfile
    public long getTotalClusterTime(HwBatterySipper sipper) {
        long totalTime = 0;
        if (this.mPowerProfile != null) {
            BatteryStats.Uid u = sipper.getBatterySipper().uidObj;
            int numClusters = this.mPowerProfile.getNumCpuClusters();
            for (int cluster = 0; cluster < numClusters; cluster++) {
                int speedsForCluster = this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
                for (int speed = 0; speed < speedsForCluster; speed++) {
                    totalTime += u.getTimeAtCpuSpeed(cluster, speed, 2);
                }
            }
        }
        return totalTime;
    }

    @Override // com.huawei.systemmanager.power.IHwPowerProfile
    public double getCpuPowerMaMs(HwBatterySipper sipper, long totalTime) {
        double cpuPowerMaMs = 0.0d;
        if (this.mPowerProfile != null) {
            BatteryStats.Uid u = sipper.getBatterySipper().uidObj;
            int i = 2;
            long cpuTimeMs = (u.getUserCpuTimeUs(2) + u.getSystemCpuTimeUs(2)) / 1000;
            int numClusters = this.mPowerProfile.getNumCpuClusters();
            int cluster = 0;
            while (cluster < numClusters) {
                int speedsForCluster = this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
                int speed = 0;
                while (speed < speedsForCluster) {
                    cpuTimeMs = cpuTimeMs;
                    cpuPowerMaMs += ((double) cpuTimeMs) * (((double) u.getTimeAtCpuSpeed(cluster, speed, i)) / ((double) totalTime)) * this.mPowerProfile.getAveragePowerForCpuCore(cluster, speed);
                    speed++;
                    u = u;
                    i = 2;
                }
                cluster++;
                i = 2;
            }
        }
        return cpuPowerMaMs;
    }

    @Override // com.huawei.systemmanager.power.IHwPowerProfile
    public double getMinAveragePowerForCpu() {
        PowerProfile powerProfile = this.mPowerProfile;
        if (powerProfile != null) {
            return powerProfile.getAveragePowerForCpuCore(0, 0);
        }
        return 0.0d;
    }

    @Override // com.huawei.systemmanager.power.IHwPowerProfile
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
