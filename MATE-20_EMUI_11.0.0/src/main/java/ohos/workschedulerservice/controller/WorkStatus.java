package ohos.workschedulerservice.controller;

import android.os.SystemClock;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.workscheduler.WorkInfo;

public final class WorkStatus {
    public static final long ACTIVE_DELAY_TIME = 0;
    public static final long APP_LISTENER = 2;
    public static final long BATTERY_LISTENER = 4;
    private static final int CONDITION_BATTERY = 16;
    private static final int CONDITION_CHARGE = 4;
    private static final int CONDITION_CONNECTIVITY = 1;
    private static final int CONDITION_DEVICE_IDLE = 2;
    private static final int CONDITION_REPEAT = 8;
    private static final int CONDITION_STORAGE = 32;
    private static final int CONDITION_TIMER = 64;
    public static final long FREQUENT_DELAY_TIME = 28800000;
    private static final int INTEREST_CONDITION = 127;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkStatus");
    private static final long MAX_BACKGROUGND_TIME = 180000;
    public static final long NETWORK_LISTENER = 3;
    public static final long NEVER_DELAY_TIME = 172800000;
    private static final int PERCENT_BASE = 100;
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_FOREGROUND_CA = 40;
    public static final int PRIORITY_RUNNING_ALMOSTTIMEOUT = -50;
    public static final int PRIORITY_RUNNING_CANCELLED = 20;
    public static final int PRIORITY_RUNNING_FASTLY = 10;
    public static final int PRIORITY_RUNNING_HALFTIMEOUT = -10;
    public static final int PRIORITY_RUNNING_NEARLYTIMEOUT = -30;
    public static final int PRIORITY_RUNNING_TIMEOUT = -90;
    public static final int PRIORITY_TOBE_RUNNING = 30;
    public static final long RARE_DELAY_TIME = 86400000;
    private static final int SCORE_RUNNING_ALMOSTTIMEOUT = 100;
    private static final int SCORE_RUNNING_FASTLY = 5;
    private static final int SCORE_RUNNING_HALFTIMEOUT = 50;
    private static final int SCORE_RUNNING_NEARLYTIMEOUT = 80;
    private static final int STORAGE_LEVEL_LOW = 1;
    public static final long STORAGE_LISTENER = 0;
    public static final long TIMER_LISTENER = 5;
    public static final long USER_LISTENER = 1;
    public static final long WORKING_DELAY_TIME = 7200000;
    public static final int WORK_IS_CANCELLED = 5;
    public static final int WORK_IS_CANCELLINIG = 3;
    public static final int WORK_IS_PENDING = 0;
    public static final int WORK_IS_RUNNING = 1;
    public static final int WORK_IS_SYSTEMCANCEL = 4;
    public static final int WORK_IS_WAITTING = 2;
    private int activeLevel;
    private final String bundleName;
    private long earliestRunTime;
    private int idleWaitTime;
    private boolean isDelayed;
    private boolean isWaited;
    private final Object lock = new Object();
    private int repeatCounter;
    private int repeatLimit;
    private int runTimes;
    private int runTimesLimit;
    private int satisfiedStatus;
    private long startTrackingTime;
    private int triggerConditions;
    private int triggerFailedNum;
    private final int uid;
    private final int userID;
    private final WorkInfo work;
    private int workPriority;
    private int workRunningState;

    private WorkStatus(WorkInfo workInfo, int i, int i2, int i3) {
        this.work = workInfo;
        this.uid = i2;
        this.userID = i3;
        this.bundleName = workInfo.getBundleName();
        this.workPriority = 0;
        this.workRunningState = 5;
        this.startTrackingTime = 0;
        this.satisfiedStatus = 0;
        this.triggerFailedNum = i;
        this.activeLevel = 0;
        this.idleWaitTime = workInfo.getRequestIdleWaitTime();
        this.repeatLimit = workInfo.getRepeatCounter();
        this.repeatCounter = 0;
        this.earliestRunTime = 0;
        if (workInfo.isRequestRepeat()) {
            this.earliestRunTime = SystemClock.elapsedRealtime() + workInfo.getRepeatCycleTime();
        }
        this.isDelayed = false;
        this.isWaited = false;
        this.triggerConditions = getRequestStatusForWork(workInfo);
        this.runTimesLimit = 0;
        this.runTimes = 0;
        if (!onlyHasRepeatCondition()) {
            this.runTimesLimit = 1;
        }
    }

    private boolean updatePriority(int i) {
        this.workPriority = (this.workPriority + i) / 2;
        return true;
    }

    public static WorkStatus generateWorkStatus(WorkInfo workInfo, int i, int i2) {
        if (workInfo == null) {
            return null;
        }
        return new WorkStatus(workInfo, 0, i, i2);
    }

    public void markCurrentState(int i) {
        if (i >= 0 && i <= 5) {
            if (i == 1 && this.startTrackingTime == 0) {
                this.startTrackingTime = SystemClock.elapsedRealtime();
            }
            if (this.workRunningState == 1 && this.startTrackingTime != 0 && (i == 2 || i == 3)) {
                int ceil = (int) Math.ceil((((double) (SystemClock.elapsedRealtime() - this.startTrackingTime)) * 100.0d) / 180000.0d);
                updatePriority(ceil <= 5 ? 10 : ceil <= SCORE_RUNNING_HALFTIMEOUT ? -10 : ceil <= SCORE_RUNNING_NEARLYTIMEOUT ? -30 : ceil < 100 ? -50 : -90);
                this.startTrackingTime = 0;
            }
            if (this.workRunningState == 0 && i == 4) {
                updatePriority(20);
            }
            this.workRunningState = i;
            HiLog.debug(LOG_LABEL, "update state: %{public}d %{public}d, new state %{public}d, priority %{public}d !", Integer.valueOf(this.uid), Integer.valueOf(this.work.getCurrentWorkID()), Integer.valueOf(i), Integer.valueOf(this.workPriority));
        }
    }

    public int getCurrentState() {
        return this.workRunningState;
    }

    public int getActiveLevel() {
        return this.activeLevel;
    }

    public void updateActiveLevel(int i) {
        if (i < 0 || i > 4) {
            HiLog.info(LOG_LABEL, "updateActiveLevel failed.", new Object[0]);
        } else {
            this.activeLevel = i;
        }
    }

    public boolean hasConnectivityCondition() {
        return (this.triggerConditions & 1) != 0;
    }

    public boolean hasDeviceIdleCondition() {
        return (this.triggerConditions & 2) != 0;
    }

    public boolean hasChargeCondition() {
        return (this.triggerConditions & 4) != 0;
    }

    public boolean hasRepeatCondition() {
        return (this.triggerConditions & 8) != 0;
    }

    public boolean onlyHasRepeatCondition() {
        return !hasRepeatCondition() || (this.triggerConditions & INTEREST_CONDITION) == 8;
    }

    public boolean hasBatteryCondition() {
        return (this.triggerConditions & 16) != 0;
    }

    public boolean hasPersistCondition() {
        return this.work.isRequestPersisted();
    }

    public boolean hasStorageCondition() {
        return (this.triggerConditions & 32) != 0;
    }

    public boolean hasDelayTimeCondition() {
        return (this.triggerConditions & 8) != 0;
    }

    public int getNetworkType() {
        return this.work.getRequestNetworkType();
    }

    public int getStorageType() {
        return this.work.getRequestStorageType();
    }

    public int getIdleWaitTime() {
        return this.idleWaitTime;
    }

    public boolean isDelay() {
        return this.isDelayed;
    }

    public void setDelay(boolean z) {
        this.isDelayed = z;
    }

    public boolean isWait() {
        return this.isWaited;
    }

    public void setWait(boolean z) {
        this.isWaited = z;
    }

    private int getRequestStatusForWork(WorkInfo workInfo) {
        int i = workInfo.isRequestNetwork() ? 1 : 0;
        if (workInfo.isRequestDeepIdle()) {
            i |= 2;
        }
        if (workInfo.isRequestCharging()) {
            i |= 4;
        }
        if (workInfo.isRequestRepeat()) {
            i |= 8;
        }
        if (workInfo.isRequestBattery()) {
            i |= 16;
        }
        if (workInfo.isRequestStorage()) {
            i |= 32;
        }
        return workInfo.isRequestDelay() ? i | 8 : i;
    }

    public WorkInfo getWork() {
        return this.work;
    }

    public int getWorkId() {
        return this.work.getCurrentWorkID();
    }

    public int getFailedNum() {
        return this.triggerFailedNum;
    }

    public int getRequestStatus() {
        return this.triggerConditions;
    }

    public long getEarliestRunTime() {
        return this.earliestRunTime;
    }

    public void updateEarliestRunTime(long j) {
        this.earliestRunTime = SystemClock.elapsedRealtime() + j;
    }

    public long getLatestRunTime() {
        return this.earliestRunTime + 1200000;
    }

    public int getUid() {
        return this.uid;
    }

    public long getStartTrackingTime() {
        return this.startTrackingTime;
    }

    public int getUserId() {
        return this.userID;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public int getPriority() {
        return this.workPriority;
    }

    public int getSatisfiedStatus() {
        int i;
        synchronized (this.lock) {
            i = this.satisfiedStatus;
        }
        return i;
    }

    public boolean changeStorageSatisfiedCondition(boolean z) {
        synchronized (this.lock) {
            int i = 32;
            boolean z2 = (this.satisfiedStatus & 32) != 0;
            boolean z3 = (this.work.getRequestStorageType() == 1) == z;
            if (z2 == z3) {
                return false;
            }
            int i2 = this.satisfiedStatus & -33;
            if (!z3) {
                i = 0;
            }
            this.satisfiedStatus = i2 | i;
            return true;
        }
    }

    public boolean changeIdleCondition(boolean z) {
        synchronized (this.lock) {
            int i = 2;
            boolean z2 = (this.satisfiedStatus & 2) != 0;
            boolean z3 = this.work.isRequestDeepIdle() == z;
            if (z2 == z3) {
                return false;
            }
            int i2 = this.satisfiedStatus & -3;
            if (!z3) {
                i = 0;
            }
            this.satisfiedStatus = i2 | i;
            return true;
        }
    }

    public boolean changeNetWorkSatisfiedCondition(int i) {
        if ((i & this.work.getRequestNetworkType()) != 0) {
            return changeSatisfiedCondition(1, true);
        }
        return changeSatisfiedCondition(1, false);
    }

    public boolean changeChargingSatisfiedCondition(boolean z, int i) {
        if ((i & this.work.getRequestChargeType()) == 0 || !z) {
            return changeSatisfiedCondition(4, false);
        }
        return changeSatisfiedCondition(4, true);
    }

    public boolean changeBatteryLevelSatisfiedCondition(int i) {
        if (this.work.getRequestBatteryLevel() == 0) {
            return false;
        }
        if (this.work.getRequestBatteryLevel() == i) {
            return changeSatisfiedCondition(16, true);
        }
        return changeSatisfiedCondition(16, false);
    }

    public boolean changeBatteryStatusSatisfiedCondition(int i) {
        if (this.work.getRequestBatteryStatus() == 0) {
            return false;
        }
        if ((i & this.work.getRequestBatteryStatus()) != 0) {
            return changeSatisfiedCondition(16, true);
        }
        return changeSatisfiedCondition(16, false);
    }

    public boolean changeTimingRepeatSatisfiedCondition(boolean z) {
        this.repeatCounter++;
        updateEarliestRunTime(this.work.getRepeatCycleTime());
        return changeSatisfiedCondition(8, z);
    }

    public boolean isRepeatOutTimes() {
        HiLog.debug(LOG_LABEL, "isRepeatOutTimes repeatCounter: %{public}d, repeatLimit: %{public}d", Integer.valueOf(this.repeatCounter), Integer.valueOf(this.repeatLimit));
        int i = this.repeatLimit;
        return i != 0 && (i < 0 || this.repeatCounter >= i);
    }

    public boolean isRunOutTimes() {
        int i;
        HiLog.debug(LOG_LABEL, "isRunOutTimes runTimes: %{public}s, runTimesLimit: %{public}s", Integer.valueOf(this.runTimes), Integer.valueOf(this.runTimesLimit));
        int i2 = this.runTimesLimit;
        return i2 != 0 && ((i = this.runTimes) < 0 || i >= i2);
    }

    public void updateRepeatCounter(int i) {
        if (i >= 0) {
            this.repeatCounter = i;
        }
    }

    public void updateRepeatLimit(int i) {
        if (i >= 0) {
            this.repeatLimit = i;
        }
    }

    public void updateRunTimes(int i) {
        if (i >= 0) {
            this.runTimes = i;
            HiLog.debug(LOG_LABEL, "updateRunTimes new runTimes: %{public}d", Integer.valueOf(this.runTimes));
        }
    }

    public void updateRunLimit(int i) {
        if (i >= 0) {
            this.runTimesLimit = i;
        }
    }

    private boolean changeSatisfiedCondition(int i, boolean z) {
        synchronized (this.lock) {
            if (((this.satisfiedStatus & i) != 0) == z) {
                return false;
            }
            int i2 = this.satisfiedStatus & (~i);
            if (!z) {
                i = 0;
            }
            this.satisfiedStatus = i | i2;
            return true;
        }
    }

    public boolean isSatisfied(int i) {
        boolean z;
        synchronized (this.lock) {
            z = (this.satisfiedStatus & i) != 0;
        }
        return z;
    }

    public boolean isReady() {
        boolean z;
        synchronized (this.lock) {
            if (!onlyHasRepeatCondition()) {
                boolean z2 = (this.satisfiedStatus & -9) == (this.triggerConditions & -9);
                boolean z3 = (this.satisfiedStatus & 8) == 8;
                boolean z4 = (this.satisfiedStatus & this.triggerConditions) == this.triggerConditions;
                if (!z2 && !z3) {
                    if (!z4) {
                        z = false;
                        HiLog.debug(LOG_LABEL, "check in not only repeat work!", new Object[0]);
                    }
                }
                z = true;
                HiLog.debug(LOG_LABEL, "check in not only repeat work!", new Object[0]);
            } else {
                z = (this.satisfiedStatus & this.triggerConditions) == this.triggerConditions;
                HiLog.debug(LOG_LABEL, "check in only repeat work or no repeat work!", new Object[0]);
            }
        }
        HiLog.debug(LOG_LABEL, "WorkID: %{public}s, isReady: %{public}s", Integer.valueOf(this.work.getCurrentWorkID()), Boolean.valueOf(z));
        return z;
    }

    public boolean matches(int i, int i2) {
        return this.work.getCurrentWorkID() == i2 && this.uid == i;
    }

    public void clearSatisfiedStatus() {
        synchronized (this.lock) {
            this.satisfiedStatus = 0;
        }
        this.isDelayed = false;
        this.isWaited = false;
    }
}
