package com.huawei.systemmanager.power;

import android.content.Context;
import android.os.Bundle;
import com.android.internal.os.BatteryStatsHelper;

class HwBatteryStatsImpl implements IBatteryStats {
    private static final long STEP_LEVEL_TIME_MASK = 1099511627775L;
    private static HwBatteryStatsImpl sInstance;
    private BatteryStatsHelper mBatteryStatsHelper;
    private IHwPowerProfile mLocalPowerProfile;

    public HwBatteryStatsImpl(Context context, boolean collectBatteryBroadcast) {
        this.mBatteryStatsHelper = new BatteryStatsHelper(context, collectBatteryBroadcast);
    }

    public static synchronized IBatteryStats get(Context context, boolean collectBatteryBroadcast) {
        synchronized (HwBatteryStatsImpl.class) {
            HwBatteryStatsImpl tmp = new HwBatteryStatsImpl(context, collectBatteryBroadcast);
            tmp.create();
            if (tmp.mBatteryStatsHelper == null) {
                return null;
            }
            return tmp;
        }
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public synchronized IHwPowerProfile getIHwPowerProfile() {
        if (this.mLocalPowerProfile == null && this.mBatteryStatsHelper != null) {
            this.mLocalPowerProfile = new HwPowerProfileImpl(this.mBatteryStatsHelper.getPowerProfile());
        }
        return this.mLocalPowerProfile;
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public BatteryStatsHelper getInnerBatteryStatsHelper() {
        return this.mBatteryStatsHelper;
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public long getTimeOfItem(long rawRealTime, int index) {
        BatteryStatsHelper batteryStatsHelper = this.mBatteryStatsHelper;
        if (batteryStatsHelper == null || batteryStatsHelper.getStats() == null) {
            return 0;
        }
        switch (index) {
            case 0:
                return this.mBatteryStatsHelper.getStats().getWifiOnTime(rawRealTime, 0);
            case 1:
                return this.mBatteryStatsHelper.getStats().computeBatteryRealtime(rawRealTime, 0) - this.mBatteryStatsHelper.getStats().getScreenOnTime(rawRealTime, 0);
            case 2:
                return getRadioTime(rawRealTime);
            case 3:
                return this.mBatteryStatsHelper.getStats().getPhoneOnTime(rawRealTime, 0);
            case 4:
                return this.mBatteryStatsHelper.getStats().getScreenOnTime(rawRealTime, 0);
            case 5:
                return this.mBatteryStatsHelper.getStats().getPhoneSignalScanningTime(rawRealTime, 0);
            case 6:
                return this.mBatteryStatsHelper.getStats().computeBatteryRealtime(rawRealTime, 2);
            case 7:
                return this.mBatteryStatsHelper.getStats().getBluetoothScanTime(rawRealTime, 0);
            case 8:
                return this.mBatteryStatsHelper.getStats().computeChargeTimeRemaining(rawRealTime);
            default:
                return 0;
        }
    }

    private long getRadioTime(long rawRealTime) {
        long signalTimeMs = 0;
        BatteryStatsHelper batteryStatsHelper = this.mBatteryStatsHelper;
        if (batteryStatsHelper == null || batteryStatsHelper.getStats() == null) {
            return 0;
        }
        for (int i = 0; i < 6; i++) {
            signalTimeMs += this.mBatteryStatsHelper.getStats().getPhoneSignalStrengthTime(i, rawRealTime, 0);
        }
        return signalTimeMs;
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public void create() {
        if (this.mBatteryStatsHelper != null) {
            this.mBatteryStatsHelper.create(new Bundle());
        }
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public void init() {
        BatteryStatsHelper batteryStatsHelper = this.mBatteryStatsHelper;
        if (batteryStatsHelper != null) {
            batteryStatsHelper.clearStats();
            this.mBatteryStatsHelper.getStats();
        }
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public long computeTimePerLevel() {
        BatteryStatsHelper batteryStatsHelper = this.mBatteryStatsHelper;
        if (batteryStatsHelper == null || batteryStatsHelper.getStats() == null) {
            return 0;
        }
        int numSteps = this.mBatteryStatsHelper.getStats().getDischargeLevelStepTracker().mNumStepDurations;
        long[] steps = this.mBatteryStatsHelper.getStats().getDischargeLevelStepTracker().mStepDurations;
        if (numSteps <= 0) {
            return -1;
        }
        if (numSteps == 1) {
            return steps[0] & STEP_LEVEL_TIME_MASK;
        }
        if (numSteps == 2) {
            return ((steps[0] & STEP_LEVEL_TIME_MASK) + (steps[1] & STEP_LEVEL_TIME_MASK)) / 2;
        }
        int former = numSteps / 3;
        int middle = (numSteps * 2) / 3;
        long totalMiddle = 0;
        long totalLatter = 0;
        long totalFormer = 0;
        for (int i = 0; i < former; i++) {
            totalFormer += steps[i] & STEP_LEVEL_TIME_MASK;
        }
        for (int j = former; j < middle; j++) {
            totalMiddle += steps[j] & STEP_LEVEL_TIME_MASK;
        }
        for (int k = middle; k < numSteps; k++) {
            totalLatter += steps[k] & STEP_LEVEL_TIME_MASK;
        }
        return (totalFormer / (((long) former) * 2)) + ((3 * totalMiddle) / (((long) (middle - former)) * 10)) + (totalLatter / (((long) (numSteps - middle)) * 5));
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public boolean startIteratingHistoryLocked() {
        BatteryStatsHelper batteryStatsHelper = this.mBatteryStatsHelper;
        if (batteryStatsHelper == null || batteryStatsHelper.getStats() == null) {
            return false;
        }
        return this.mBatteryStatsHelper.getStats().startIteratingHistoryLocked();
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public boolean getNextHistoryLocked(HwHistoryItem out) {
        BatteryStatsHelper batteryStatsHelper;
        if (out == null || out.getInnerHistoryItem() == null || (batteryStatsHelper = this.mBatteryStatsHelper) == null || batteryStatsHelper.getStats() == null) {
            return false;
        }
        return this.mBatteryStatsHelper.getStats().getNextHistoryLocked(out.getInnerHistoryItem());
    }

    @Override // com.huawei.systemmanager.power.IBatteryStats
    public void finishIteratingHistoryLocked() {
        BatteryStatsHelper batteryStatsHelper = this.mBatteryStatsHelper;
        if (batteryStatsHelper != null && batteryStatsHelper.getStats() != null) {
            this.mBatteryStatsHelper.getStats().finishIteratingHistoryLocked();
        }
    }
}
