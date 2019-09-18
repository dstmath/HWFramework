package com.huawei.systemmanager.power;

import android.content.Context;
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

    public synchronized IHwPowerProfile getIHwPowerProfile() {
        if (this.mLocalPowerProfile == null && this.mBatteryStatsHelper != null) {
            this.mLocalPowerProfile = new HwPowerProfileImpl(this.mBatteryStatsHelper.getPowerProfile());
        }
        return this.mLocalPowerProfile;
    }

    public BatteryStatsHelper getInnerBatteryStatsHelper() {
        return this.mBatteryStatsHelper;
    }

    public long getTimeOfItem(long rawRealTime, int index) {
        long rettime = 0;
        if (this.mBatteryStatsHelper == null || this.mBatteryStatsHelper.getStats() == null) {
            return 0;
        }
        switch (index) {
            case 0:
                rettime = this.mBatteryStatsHelper.getStats().getWifiOnTime(rawRealTime, 0);
                break;
            case 1:
                rettime = this.mBatteryStatsHelper.getStats().computeBatteryRealtime(rawRealTime, 0) - this.mBatteryStatsHelper.getStats().getScreenOnTime(rawRealTime, 0);
                break;
            case 2:
                rettime = getRadioTime(rawRealTime);
                break;
            case 3:
                rettime = this.mBatteryStatsHelper.getStats().getPhoneOnTime(rawRealTime, 0);
                break;
            case 4:
                rettime = this.mBatteryStatsHelper.getStats().getScreenOnTime(rawRealTime, 0);
                break;
            case 5:
                rettime = this.mBatteryStatsHelper.getStats().getPhoneSignalScanningTime(rawRealTime, 0);
                break;
            case 6:
                rettime = this.mBatteryStatsHelper.getStats().computeBatteryRealtime(rawRealTime, 2);
                break;
            case 7:
                rettime = this.mBatteryStatsHelper.getStats().getBluetoothScanTime(rawRealTime, 0);
                break;
            case 8:
                rettime = this.mBatteryStatsHelper.getStats().computeChargeTimeRemaining(rawRealTime);
                break;
        }
        return rettime;
    }

    private long getRadioTime(long rawRealTime) {
        if (this.mBatteryStatsHelper == null || this.mBatteryStatsHelper.getStats() == null) {
            return 0;
        }
        long signalTimeMs = 0;
        for (int i = 0; i < 5; i++) {
            signalTimeMs += this.mBatteryStatsHelper.getStats().getPhoneSignalStrengthTime(i, rawRealTime, 0);
        }
        return signalTimeMs;
    }

    public void create() {
        if (this.mBatteryStatsHelper != null) {
            this.mBatteryStatsHelper.create(null);
        }
    }

    public void init() {
        if (this.mBatteryStatsHelper != null) {
            this.mBatteryStatsHelper.clearStats();
            this.mBatteryStatsHelper.getStats();
        }
    }

    public long computeTimePerLevel() {
        long total;
        if (this.mBatteryStatsHelper == null || this.mBatteryStatsHelper.getStats() == null) {
            return 0;
        }
        int numSteps = this.mBatteryStatsHelper.getStats().getDischargeLevelStepTracker().mNumStepDurations;
        long[] steps = this.mBatteryStatsHelper.getStats().getDischargeLevelStepTracker().mStepDurations;
        if (numSteps <= 0) {
            return -1;
        }
        if (numSteps == 1) {
            total = steps[0] & STEP_LEVEL_TIME_MASK;
        } else if (numSteps == 2) {
            total = ((steps[0] & STEP_LEVEL_TIME_MASK) + (steps[1] & STEP_LEVEL_TIME_MASK)) / 2;
        } else {
            int former = numSteps / 3;
            int middle = (numSteps * 2) / 3;
            long total_former = 0;
            long total_middle = 0;
            long total_latter = 0;
            for (int i = 0; i < former; i++) {
                total_former += steps[i] & STEP_LEVEL_TIME_MASK;
            }
            for (int j = former; j < middle; j++) {
                total_middle += steps[j] & STEP_LEVEL_TIME_MASK;
            }
            for (int k = middle; k < numSteps; k++) {
                total_latter += steps[k] & STEP_LEVEL_TIME_MASK;
            }
            long[] jArr = steps;
            total = (total_former / (2 * ((long) former))) + ((3 * total_middle) / (10 * ((long) (middle - former)))) + (total_latter / (5 * ((long) (numSteps - middle))));
            return total;
        }
        return total;
    }

    public boolean startIteratingHistoryLocked() {
        if (this.mBatteryStatsHelper == null || this.mBatteryStatsHelper.getStats() == null) {
            return false;
        }
        return this.mBatteryStatsHelper.getStats().startIteratingHistoryLocked();
    }

    public boolean getNextHistoryLocked(HwHistoryItem out) {
        if (this.mBatteryStatsHelper == null || this.mBatteryStatsHelper.getStats() == null) {
            return false;
        }
        return this.mBatteryStatsHelper.getStats().getNextHistoryLocked(out.getInnerHistoryItem());
    }

    public void finishIteratingHistoryLocked() {
        if (this.mBatteryStatsHelper != null && this.mBatteryStatsHelper.getStats() != null) {
            this.mBatteryStatsHelper.getStats().finishIteratingHistoryLocked();
        }
    }
}
