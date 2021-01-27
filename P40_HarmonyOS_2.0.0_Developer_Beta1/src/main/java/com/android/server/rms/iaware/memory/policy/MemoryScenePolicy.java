package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.action.KillAction;
import com.android.server.rms.iaware.memory.action.QuickKillAction;
import com.android.server.rms.iaware.memory.action.ReclaimAction;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.huawei.server.rme.hyperhold.Swap;
import java.util.ArrayList;
import java.util.List;

public class MemoryScenePolicy {
    private static final int CPU_LOAD_FACTOR = 6;
    private static final int CPU_LOAD_THRESHOLD = 100;
    private static final int STEP_COUNT_THRESHOLD = 15;
    private static final String TAG = "AwareMem_MemPolicy";
    private final List<Action> mActions;
    private volatile Bundle mExtras;
    private final String mName;

    public MemoryScenePolicy(String name, List<Action> actionList) {
        if (!Swap.getInstance().isSwapEnabled()) {
            this.mActions = actionList;
        } else {
            this.mActions = new ArrayList();
            if (actionList != null) {
                for (Action action : actionList) {
                    if (!action.getClass().equals(KillAction.class) && !action.getClass().equals(QuickKillAction.class) && !action.getClass().equals(ReclaimAction.class)) {
                        this.mActions.add(action);
                    }
                }
            }
        }
        this.mName = name;
    }

    public String getSceneName() {
        return this.mName;
    }

    public void setExtras(Bundle extras) {
        this.mExtras = extras;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public int execute() {
        Bundle tmpExtras = this.mExtras;
        if (tmpExtras == null) {
            return -1;
        }
        long reqMem = tmpExtras.getLong("reqMem");
        if (reqMem <= 0) {
            AwareLog.w(TAG, "Execute memorypolicy exit, because of reqMem is negative:" + reqMem);
            return -1;
        } else if (reqMem > MemoryConstant.getMaxReqMem()) {
            AwareLog.w(TAG, "Execute memorypolicy exit, because of reqMem is too big:" + reqMem);
            return -1;
        } else {
            List<Action> list = this.mActions;
            if (list == null || list.isEmpty()) {
                AwareLog.w(TAG, "Memorypolicy actions is empty");
                return -1;
            }
            int result = 0;
            long start = SystemClock.elapsedRealtime();
            for (Action action : this.mActions) {
                result |= action.execute(tmpExtras);
            }
            long end = SystemClock.elapsedRealtime();
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "Execute memorypolicy use: " + (end - start) + " ms");
            }
            return result;
        }
    }

    public void clear() {
        AwareLog.d(TAG, "clear memorypolicy");
        this.mExtras = null;
        forceInterrupt(false);
    }

    public void reset() {
        AwareLog.d(TAG, "reset memorypolicy");
        this.mExtras = null;
        forceInterrupt(true);
        List<Action> list = this.mActions;
        if (!(list == null || list.isEmpty())) {
            for (Action action : this.mActions) {
                action.reset();
            }
        }
    }

    public boolean canBeExecuted() {
        List<Action> list = this.mActions;
        if (list == null || list.isEmpty()) {
            AwareLog.w(TAG, "Memorypolicy canBeExecuted: actions is empty");
            return false;
        }
        for (Action action : this.mActions) {
            if (!action.canBeExecuted()) {
                return false;
            }
        }
        return true;
    }

    public long getPollingPeriod() {
        List<Action> list = this.mActions;
        if (list == null || list.isEmpty()) {
            return MemoryConstant.getDefaultTimerPeriod();
        }
        int maxStepCount = getPollingStepCount();
        if (maxStepCount <= 0) {
            return getMemPressure();
        }
        if (maxStepCount < STEP_COUNT_THRESHOLD) {
            return (long) (((double) MemoryConstant.getDefaultTimerPeriod()) * Math.pow(2.0d, (double) maxStepCount));
        }
        return MemoryConstant.getMaxTimerPeriod();
    }

    private int getPollingStepCount() {
        int maxStepCount = 0;
        for (Action action : this.mActions) {
            int stepCount = action.getLastExecFailCount();
            if (action.getLastExecFailCount() < 1) {
                return 0;
            }
            maxStepCount = maxStepCount < stepCount ? stepCount : maxStepCount;
        }
        return maxStepCount;
    }

    private long getMemPressure() {
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.w(TAG, "calcMemPressure read availableRam err!" + availableRam);
            return MemoryConstant.getDefaultTimerPeriod();
        } else if (availableRam >= MemoryConstant.getIdleMemory()) {
            return MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
        } else {
            long minInterval = MemoryConstant.getMinTimerPeriod();
            if (availableRam <= MemoryConstant.getEmergencyMemory()) {
                return minInterval;
            }
            long interval = (MemoryConstant.MIN_INTERVAL_OP_TIMEOUT * (availableRam - MemoryConstant.getEmergencyMemory())) / (MemoryConstant.getIdleMemory() - MemoryConstant.getEmergencyMemory());
            return interval < minInterval ? minInterval : interval;
        }
    }

    public long getCpuPressure(long availableRam, long cpuThresHold, long sysCpuOverLoadCnt) {
        if (availableRam < MemoryConstant.getEmergencyMemory() || availableRam >= MemoryConstant.getCriticalMemory()) {
            return cpuThresHold;
        }
        long curCpuLoad = (((MemoryConstant.getCriticalMemory() - availableRam) * (100 - cpuThresHold)) / (MemoryConstant.getCriticalMemory() - MemoryConstant.getEmergencyMemory())) + cpuThresHold + (6 * sysCpuOverLoadCnt);
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "getCpuPressure curCpuLoad=" + curCpuLoad + " sysCpuOverLoadCnt:" + sysCpuOverLoadCnt);
        }
        return curCpuLoad;
    }

    public boolean interrupt(boolean forced) {
        AwareLog.i(TAG, "Interrupt memorypolicy: forced=" + forced);
        if (!forced) {
            return reqInterrupt();
        }
        forceInterrupt(true);
        return true;
    }

    private void forceInterrupt(boolean interrupted) {
        List<Action> list = this.mActions;
        if (!(list == null || list.isEmpty())) {
            for (Action action : this.mActions) {
                action.interrupt(interrupted);
            }
        }
    }

    private boolean reqInterrupt() {
        List<Action> list = this.mActions;
        if (list == null || list.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (Action action : this.mActions) {
            if (!action.reqInterrupt(this.mExtras)) {
                result = false;
            }
        }
        return result;
    }
}
