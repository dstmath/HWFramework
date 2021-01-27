package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PressureDetector;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class IdleMemoryExecutor extends AbsMemoryExecutor {
    private static final int CONTINUOUS_LOW_MEM_COUNT_THRESHOLD = 3;
    private static final int MSG_CYCLE_RECYCLE = 11;
    private static final int PROTECTLRU_MIN_TIME_GAP = 600000;
    private static final int RECLAIM_FACTOR_DO_NOTHING = 0;
    private static final int RECLAIM_FACTOR_SWAP_BALANCED = 3;
    private static final int RECLAIM_FACTOR_SWAP_LESS = 2;
    private static final int RECLAIM_FACTOR_SWAP_MORE = 1;
    private static final int SYSTEM_MEMORY_STATE_BASE = 0;
    private static final int SYSTEM_MEMORY_STATE_LOW = 1;
    private static final int SYSTEM_MEMORY_STATE_NORMAL = 0;
    private static final String TAG = "AwareMem_IdleMemExec";
    private static final long THREAD_POOL_ALIVE_TIME = 30;
    private static final String UNIT_KB = "KB";
    private BigDataStore mBigDataStore;
    private ThreadPoolExecutor mIdleMemAppExecutor;
    private ActionFlag mLastExecutorActionFlag;
    private MemHandler mMemHandler;
    private PressureDetector mPressureDetector;
    private int mProtectCacheFlag;
    private long mProtectCacheTimestamp;
    private int mSwapLessTimes;
    private long mSysCpuOverLoadCnt;
    private LimitedSizeQueue<Integer> mSysLowMemStateTracker;

    /* access modifiers changed from: private */
    public enum ActionFlag {
        ACTION_NONE,
        ACTION_RECLAIM,
        ACTION_KILL
    }

    public IdleMemoryExecutor() {
        this.mIdleMemAppExecutor = null;
        this.mBigDataStore = BigDataStore.getInstance();
        this.mSwapLessTimes = 0;
        this.mSysLowMemStateTracker = null;
        this.mLastExecutorActionFlag = ActionFlag.ACTION_NONE;
        this.mIdleMemAppExecutor = new ThreadPoolExecutor(0, 1, (long) THREAD_POOL_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new AbsMemoryExecutor.MemThreadFactory("iaware.mem.default"));
        this.mProtectCacheFlag = 0;
        this.mProtectCacheTimestamp = SystemClock.uptimeMillis();
        this.mSysLowMemStateTracker = new LimitedSizeQueue<>(3);
        for (int i = 0; i < 3; i++) {
            this.mSysLowMemStateTracker.add(0);
        }
        this.mPressureDetector = PressureDetector.getInstance();
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public void disableMemoryRecover() {
        this.mMemState.setStatus(0);
        MemHandler memHandler = this.mMemHandler;
        if (memHandler != null) {
            memHandler.removeAllMessage();
        }
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public void stopMemoryRecover() {
        this.mStopAction.set(true);
        disableMemoryRecover();
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public void executeMemoryRecover(Bundle extras) {
        int event = extras.getInt("event");
        MemHandler memHandler = this.mMemHandler;
        if (memHandler != null) {
            memHandler.sendMessage(memHandler.getMessage(11, event, extras));
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "executeMemoryRecover event=" + event);
            }
        }
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public void setMemHandlerThread(HandlerThread handlerThread) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "setHandler: object=" + handlerThread);
        }
        this.mMemHandler = new MemHandler(handlerThread.getLooper());
    }

    private void executeIdleMemory(int event, Bundle extras) {
        this.mStopAction.set(false);
        MemoryScenePolicy idleMemoryScenePolicy = createPolicyByMemCpu(event, extras);
        if (idleMemoryScenePolicy == null) {
            this.mMemState.setStatus(0);
            return;
        }
        try {
            this.mIdleMemAppExecutor.execute(new AbsMemoryExecutor.CalcRunnable(idleMemoryScenePolicy, false));
        } catch (RejectedExecutionException e) {
            AwareLog.e(TAG, "Failed to execute! reject");
        } catch (Exception e2) {
            AwareLog.e(TAG, "Failed to execute! reset");
        }
    }

    private MemoryScenePolicy createPolicyByMemCpu(int event, Bundle extras) {
        MemoryScenePolicyList memoryScenePolicyList = MemoryExecutorServer.getInstance().getMemoryScenePolicyList();
        if (memoryScenePolicyList != null) {
            if (extras != null) {
                boolean immediated = extras.getBoolean("immediate", false);
                MemoryResult memResult = calculateMemStatus(immediated);
                updateSysMemStates(memResult.mAvailable);
                if (!isProtectCache() && isContLowMemState()) {
                    setProtectCache(1);
                } else if (!isUnprotectCache() && isContNormalMemState() && isProtectLruTimeOk()) {
                    setProtectCache(0);
                } else if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "Bybass set protect cache. States: " + this.mProtectCacheFlag + ", " + Arrays.toString(this.mSysLowMemStateTracker.toArray()));
                }
                if (memResult.mDecideBasedOnPressure) {
                    if (event != 15005) {
                        balanceMemoryReclaim(memResult);
                    }
                }
                if (!memResult.mDecideBasedOnPressure || MemoryConstant.getExtraDependOnPsiSwitch() != 1) {
                    updateExtraFreeKb(memResult.mAvailable);
                } else {
                    updateExtraFreeKbPressure(memResult.mMemPressureHigh);
                }
                if (memResult.mScene == null) {
                    EventTracker.getInstance().trackEvent(1001, 0, 0, memResult.mTraceInfo);
                    return null;
                }
                MemoryScenePolicy policy = memoryScenePolicyList.getMemoryScenePolicy(memResult.mScene);
                if (policy == null || (!immediated && !policy.canBeExecuted())) {
                    String reason = policy == null ? "null policy" : "policy can not execute";
                    EventTracker.getInstance().trackEvent(1001, 0, 0, memResult.mTraceInfo + " but " + reason);
                    return null;
                }
                extras.putBoolean("emergency", memResult.mIsEmergent);
                extras.putLong("available", memResult.mAvailable);
                extras.putLong("reqMem", memResult.mRequest);
                extras.putBoolean("sysPressHigh", memResult.mSysPressureHigh);
                extras.putBoolean("isFixedKillSize", memResult.mIsFixedKillSize);
                policy.setExtras(extras);
                EventTracker.getInstance().trackEvent(EventTracker.TRACK_TYPE_TRIG, 0, 0, memResult.mTraceInfo);
                return policy;
            }
        }
        AwareLog.w(TAG, "createPolicyByMemCpu memoryScenePolicyList null");
        return null;
    }

    private void updateExtraFreeKbPressure(boolean memPressureHigh) {
        if (memPressureHigh) {
            MemoryUtils.writeExtraFreeKbytes(MemoryConstant.getConfigExtraFreeKbytes());
        } else {
            MemoryUtils.writeExtraFreeKbytes(MemoryConstant.DEFAULT_EXTRA_FREE_KBYTES);
        }
    }

    private void updateExtraFreeKb(long availableRam) {
        if (availableRam > 0) {
            updateExtraFreeKbPressure(availableRam < MemoryConstant.getIdleMemory());
        }
    }

    private MemoryResult calculateMemStatus(boolean immediated) {
        MemoryResult memResult = new MemoryResult();
        if (!canRecoverExecute("calculateMemStatus")) {
            memResult.mTraceInfo = "DME not running or interrupted";
            return memResult;
        }
        this.mMemState.setStatus(1);
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.e(TAG, "calculateMemStatus read availableRam err!" + availableRam);
            memResult.mTraceInfo = "read availableRam err";
            return memResult;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "calculateMemStatus memory availableRam=" + availableRam);
        }
        memResult.mAvailable = availableRam;
        if (MemoryConstant.getPressureReclaimSwitch() == 1) {
            PressureDetector pressureDetector = this.mPressureDetector;
            if (pressureDetector == null || !pressureDetector.isAvailable()) {
                MemoryConstant.setPressureReclaimSwitch(0);
            } else {
                updatePressureStatus(memResult);
            }
        }
        updateMemStatus(memResult, availableRam, immediated);
        return memResult;
    }

    private void balanceMemoryReclaim(MemoryResult memResult) {
        int targetSwappiness;
        int targetRccAvail;
        if (memResult.mRecalimFactor != 0) {
            if (memResult.mRecalimFactor == 1) {
                this.mSwapLessTimes = 0;
                targetSwappiness = MemoryUtils.getLastSwappiness() + 10;
                if (targetSwappiness > MemoryConstant.getConfigMaxSwappiness()) {
                    targetSwappiness = MemoryConstant.getConfigMaxSwappiness();
                }
                targetRccAvail = (int) MemoryConstant.getRccHighAvailTarget();
                boolean canKernelCompress = MemoryReader.canKernelCompress();
                boolean isBigMemCriticalMemory = MemoryConstant.isBigMemCriticalMemory();
                if (MemoryConstant.isKernCompressEnable() && canKernelCompress && !isBigMemCriticalMemory) {
                    AwareLog.d(TAG, "force rcc " + MemoryConstant.getConfigEnhancedRccSize() + UNIT_KB);
                    MemoryUtils.rccCompress(MemoryConstant.getConfigEnhancedRccSize());
                }
            } else if (memResult.mRecalimFactor == 2) {
                this.mSwapLessTimes++;
                targetSwappiness = MemoryUtils.getLastSwappiness() - (this.mSwapLessTimes * 10);
                if (targetSwappiness < MemoryConstant.getConfigMinSwappiness()) {
                    targetSwappiness = MemoryConstant.getConfigMinSwappiness();
                }
                targetRccAvail = (int) (MemoryConstant.getIdleMemory() / 1024);
            } else {
                this.mSwapLessTimes = 0;
                targetSwappiness = MemoryConstant.getConfigBalanceSwappiness();
                targetRccAvail = (int) (MemoryConstant.getIdleMemory() / 1024);
            }
            MemoryUtils.writeSwappiness(targetSwappiness);
            MemoryUtils.rccSetAvailTarget(targetRccAvail);
            AwareLog.d(TAG, "balanceMemoryReclaim, targetSwappiness = " + targetSwappiness + ", mRecalimFactor = " + memResult.mRecalimFactor + ", targetRccAvail = " + targetRccAvail + ", swapLessTimes = " + this.mSwapLessTimes);
        }
    }

    private void setMemoryRecalimFactor(MemoryResult memResult) {
        if (!this.mPressureDetector.isPressureHigh(2) && !this.mPressureDetector.isPressureHigh(3)) {
            memResult.mRecalimFactor = 1;
        } else if (!this.mPressureDetector.isPressureHigh(0) && (this.mPressureDetector.isPressureHigh(2) || this.mPressureDetector.isPressureHigh(3))) {
            memResult.mRecalimFactor = 2;
        } else if (!this.mPressureDetector.isPressureHigh(0) || (!this.mPressureDetector.isPressureHigh(2) && !this.mPressureDetector.isPressureHigh(3))) {
            memResult.mRecalimFactor = 0;
        } else {
            memResult.mRecalimFactor = 3;
        }
    }

    private void updatePressureStatus(MemoryResult memResult) {
        memResult.mDecideBasedOnPressure = true;
        this.mPressureDetector.updateSystemPressure();
        if (this.mPressureDetector.isPressureHigh(0) && this.mPressureDetector.isPressureHigh(1) && this.mPressureDetector.isPressureHigh(2)) {
            memResult.mSysPressureHigh = true;
        }
        memResult.mMemPressureHigh = this.mPressureDetector.isPressureHigh(1);
        setMemoryRecalimFactor(memResult);
        AwareLog.i(TAG, "updatePressureStatus, mSysPressureHigh = " + memResult.mSysPressureHigh + ", mMemPressureHigh = " + memResult.mMemPressureHigh + ", mRecalimFactor = " + memResult.mRecalimFactor);
    }

    private void updateMemStatusImmediated(MemoryResult memResult, long availableRam) {
        this.mLastExecutorActionFlag = ActionFlag.ACTION_KILL;
        memResult.mRequest = MemoryConstant.getCriticalMemory() - availableRam;
        memResult.mScene = MemoryConstant.MEM_SCENE_DEFAULT;
        memResult.mIsEmergent = true;
        memResult.mTraceInfo = "app emergency ";
        memResult.mTraceInfo += availableRam;
    }

    private void updateMemStatusEnough(MemoryResult memResult, long availableRam) {
        memResult.mTraceInfo = "memory enough " + availableRam + UNIT_KB;
        this.mLastExecutorActionFlag = ActionFlag.ACTION_NONE;
        setBelowThresholdTime();
    }

    private void updateMemStatusIdle(MemoryResult memResult, long availableRam) {
        if (!memResult.mDecideBasedOnPressure || !memResult.mSysPressureHigh) {
            memResult.mRequest = MemoryConstant.getIdleMemory() - availableRam;
            memResult.mScene = MemoryConstant.MEM_SCENE_IDLE;
            memResult.mTraceInfo = "memory idle " + availableRam + UNIT_KB;
            this.mLastExecutorActionFlag = ActionFlag.ACTION_RECLAIM;
        } else {
            memResult.mRequest = MemoryConstant.getConfigEnhancedKillSize();
            memResult.mScene = MemoryConstant.MEM_SCENE_DEFAULT;
            memResult.mTraceInfo = "system pressure is high, memory idle " + availableRam + UNIT_KB;
            memResult.mIsFixedKillSize = true;
            this.mLastExecutorActionFlag = ActionFlag.ACTION_KILL;
        }
        setBelowThresholdTime();
    }

    private void updateMemStatusCriticalGap(MemoryResult memResult, long availableRam) {
        boolean canKernelCompress = MemoryReader.canKernelCompress();
        boolean isBigMemCriticalMemory = MemoryConstant.isBigMemCriticalMemory();
        if (memResult.mDecideBasedOnPressure && this.mLastExecutorActionFlag != ActionFlag.ACTION_KILL) {
            memResult.mRequest = MemoryConstant.getIdleMemory() - availableRam;
            memResult.mScene = MemoryConstant.MEM_SCENE_IDLE;
            memResult.mTraceInfo = "memory in idle gap " + availableRam + UNIT_KB;
            this.mLastExecutorActionFlag = ActionFlag.ACTION_RECLAIM;
        } else if (memResult.mDecideBasedOnPressure || !MemoryConstant.isKernCompressEnable() || !canKernelCompress || isBigMemCriticalMemory) {
            memResult.mRequest = MemoryConstant.getCriticalMemory() - availableRam;
            memResult.mScene = MemoryConstant.MEM_SCENE_DEFAULT;
            memResult.mTraceInfo = "memory in critical gap " + availableRam + UNIT_KB;
            this.mLastExecutorActionFlag = ActionFlag.ACTION_KILL;
        } else {
            memResult.mRequest = MemoryConstant.getIdleMemory() - availableRam;
            memResult.mScene = MemoryConstant.MEM_SCENE_IDLE;
            memResult.mTraceInfo = "memory in idle gap " + availableRam + UNIT_KB;
            this.mLastExecutorActionFlag = ActionFlag.ACTION_RECLAIM;
            MemoryUtils.rccCompress(MemoryConstant.getCriticalMemory() - availableRam);
        }
        setLowMemoryManageCount();
    }

    private void updateMemStatusCritical(MemoryResult memResult, long availableRam) {
        memResult.mRequest = MemoryConstant.getCriticalMemory() - availableRam;
        memResult.mScene = MemoryConstant.MEM_SCENE_DEFAULT;
        memResult.mIsEmergent = memResult.mSysPressureHigh || availableRam <= MemoryConstant.getEmergencyMemory();
        memResult.mTraceInfo = memResult.mIsEmergent ? "memory emergency " : "memory critical ";
        memResult.mTraceInfo += availableRam + UNIT_KB;
        this.mLastExecutorActionFlag = ActionFlag.ACTION_KILL;
        setLowMemoryManageCount();
    }

    private void updateMemStatus(MemoryResult memResult, long availableRam, boolean immediated) {
        if (immediated) {
            updateMemStatusImmediated(memResult, availableRam);
        } else if (availableRam >= MemoryConstant.getIdleMemory()) {
            updateMemStatusEnough(memResult, availableRam);
        } else if (availableRam >= MemoryConstant.getCriticalMemory()) {
            updateMemStatusIdle(memResult, availableRam);
        } else if (availableRam >= MemoryConstant.getCriticalMemory() - MemoryConstant.getReclaimKillGapMemory()) {
            updateMemStatusCriticalGap(memResult, availableRam);
        } else {
            updateMemStatusCritical(memResult, availableRam);
        }
    }

    /* access modifiers changed from: private */
    public static final class MemoryResult {
        long mAvailable;
        boolean mDecideBasedOnPressure;
        boolean mIsEmergent;
        boolean mIsFixedKillSize;
        boolean mMemPressureHigh;
        int mRecalimFactor;
        long mRequest;
        String mScene;
        boolean mSysPressureHigh;
        String mTraceInfo;

        private MemoryResult() {
            this.mRequest = 0;
            this.mAvailable = 0;
            this.mIsEmergent = false;
            this.mDecideBasedOnPressure = false;
            this.mIsFixedKillSize = false;
            this.mMemPressureHigh = false;
            this.mSysPressureHigh = false;
            this.mRecalimFactor = 0;
            this.mScene = null;
            this.mTraceInfo = null;
        }
    }

    /* access modifiers changed from: private */
    public final class MemHandler extends Handler {
        MemHandler(Looper looper) {
            super(looper);
        }

        public void removeAllMessage() {
            removeMessages(11);
        }

        public Message getMessage(int what, int arg1, Object obj) {
            return obtainMessage(what, arg1, 0, obj);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 11) {
                IdleMemoryExecutor.this.handleRecycleMsg(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRecycleMsg(Message msg) {
        Bundle extras;
        int event = msg.arg1;
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "MemHandler event=" + event);
        }
        if (msg.obj instanceof Bundle) {
            extras = (Bundle) msg.obj;
        } else {
            extras = null;
        }
        if (extras == null) {
            AwareLog.w(TAG, "MemHandler extras is null");
            return;
        }
        long timeStamp = extras.getLong("timeStamp");
        if (this.mMemState.getStatus() != 0) {
            EventTracker.getInstance().trackEvent(1001, event, timeStamp, "mem " + this.mMemState.toString());
        } else if (MemoryExecutorServer.getInstance().getBigMemAppLaunching()) {
            AwareLog.d(TAG, "MemHandler big memory app is running");
        } else {
            if (event > 0 && timeStamp > 0) {
                EventTracker.getInstance().trackEvent(1000, event, timeStamp, null);
            }
            executeIdleMemory(event, extras);
        }
    }

    private void setBelowThresholdTime() {
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            long belowThresholdTimeEnd = SystemClock.elapsedRealtime();
            if (this.mBigDataStore.belowThresholdTimeBegin > 0 && belowThresholdTimeEnd - this.mBigDataStore.belowThresholdTimeBegin > 0) {
                this.mBigDataStore.belowThresholdTime += belowThresholdTimeEnd - this.mBigDataStore.belowThresholdTimeBegin;
                this.mBigDataStore.belowThresholdTimeBegin = 0;
            }
        }
    }

    private void setLowMemoryManageCount() {
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            if (this.mBigDataStore.belowThresholdTimeBegin == 0) {
                this.mBigDataStore.belowThresholdTimeBegin = SystemClock.elapsedRealtime();
            }
            this.mBigDataStore.lowMemoryManageCount++;
        }
    }

    /* access modifiers changed from: private */
    public static final class LimitedSizeQueue<T> extends LinkedList<T> {
        private static final long serialVersionUID = 6928859904407185256L;
        private int maxSize;

        LimitedSizeQueue(int size) {
            this.maxSize = size;
        }

        @Override // java.util.LinkedList, java.util.AbstractCollection, java.util.List, java.util.Collection, java.util.AbstractList, java.util.Queue, java.util.Deque
        public boolean add(T element) {
            boolean added = super.add(element);
            while (added && size() > this.maxSize) {
                super.remove();
            }
            return added;
        }
    }

    private void updateSysMemStates(long memAvailKb) {
        int state;
        if (memAvailKb >= 0) {
            if (memAvailKb < MemoryConstant.getCriticalMemory()) {
                state = 1;
            } else {
                state = 0;
            }
            this.mSysLowMemStateTracker.add(Integer.valueOf(state));
        }
    }

    private boolean isContLowMemState() {
        for (int i = 0; i < 3; i++) {
            if (((Integer) this.mSysLowMemStateTracker.get(i)).intValue() != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isContNormalMemState() {
        for (int i = 0; i < 3; i++) {
            if (((Integer) this.mSysLowMemStateTracker.get(i)).intValue() != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isProtectCache() {
        return this.mProtectCacheFlag == 1;
    }

    private boolean isUnprotectCache() {
        return this.mProtectCacheFlag == 0;
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public int getProtectCacheFlag() {
        return this.mProtectCacheFlag;
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public void setProtectCacheFlag(int state) {
        this.mProtectCacheFlag = state;
    }

    private void setProtectCache(int state) {
        if (state == 1 || state == 0) {
            this.mProtectCacheFlag = state;
            this.mProtectCacheTimestamp = SystemClock.uptimeMillis();
            MemoryUtils.dynamicSetProtectLru(state);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "setProtectCache state: " + state + ". States: " + this.mProtectCacheFlag + ", " + Arrays.toString(this.mSysLowMemStateTracker.toArray()));
            }
        }
    }

    private boolean isProtectLruTimeOk() {
        long now = SystemClock.uptimeMillis();
        long j = this.mProtectCacheTimestamp;
        boolean ret = now > j && now - j >= AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME;
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "isProtectLruTimeOk: " + ret + ". States: " + this.mProtectCacheFlag + ", " + Arrays.toString(this.mSysLowMemStateTracker.toArray()));
        }
        return ret;
    }
}
