package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.HandlerThread;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.PollingTimer;

public class MemoryExecutorServer {
    private static final String TAG = "AwareMem_MemExecSvr";
    private static final Object mLock = new Object();
    private static MemoryExecutorServer sMemoryExecutorServer;
    boolean mBigMemAppLaunching = false;
    private final AbsMemoryExecutor mBigMemoryExecutor = new BigMemoryExecutor();
    private final AbsMemoryExecutor mIdleMemoryExecutor = new IdleMemoryExecutor();
    MemoryScenePolicyList mMemoryScenePolicyList = null;
    PollingTimer mPollingTimer = new PollingTimer();
    MemoryScenePolicy mRunningPolicy = null;

    public static MemoryExecutorServer getInstance() {
        MemoryExecutorServer memoryExecutorServer;
        synchronized (mLock) {
            if (sMemoryExecutorServer == null) {
                sMemoryExecutorServer = new MemoryExecutorServer();
            }
            memoryExecutorServer = sMemoryExecutorServer;
        }
        return memoryExecutorServer;
    }

    private MemoryExecutorServer() {
    }

    public void enable() {
        this.mIdleMemoryExecutor.enableMemoryRecover();
        this.mBigMemoryExecutor.enableMemoryRecover();
        setPollingPeriod(MemoryConstant.getDefaultTimerPeriod());
        this.mMemoryScenePolicyList.clear();
    }

    public void disable() {
        this.mPollingTimer.stopTimer();
        this.mIdleMemoryExecutor.disableMemoryRecover();
        this.mBigMemoryExecutor.disableMemoryRecover();
        this.mMemoryScenePolicyList.reset();
    }

    public void executeMemoryRecover(String scene, Bundle extras, int event, long timeStamp) {
        if (getBigMemAppLaunching()) {
            AwareLog.i(TAG, "executeMemoryRecover big mem app is running");
        } else if (extras == null || this.mMemoryScenePolicyList == null) {
            AwareLog.e(TAG, "executeMemoryRecover null policy!!");
        } else {
            extras.putInt("event", event);
            extras.putLong("timeStamp", timeStamp);
            if (MemoryConstant.MEM_SCENE_BIGMEM.equals(scene)) {
                this.mBigMemoryExecutor.executeMemoryRecover(extras);
            } else {
                this.mIdleMemoryExecutor.executeMemoryRecover(extras);
            }
        }
    }

    public void stopMemoryRecover() {
        if (getBigMemAppLaunching()) {
            AwareLog.i(TAG, "stopExecuteMemoryRecover big memory app is running");
            return;
        }
        this.mBigMemoryExecutor.stopMemoryRecover();
        this.mIdleMemoryExecutor.stopMemoryRecover();
        stopMemoryPolicy(false);
    }

    public void setMemHandlerThread(HandlerThread handlerThread) {
        if (handlerThread != null) {
            this.mIdleMemoryExecutor.setMemHandlerThread(handlerThread);
            this.mPollingTimer.setPollingTimerHandler(handlerThread);
            AwareLog.d(TAG, "setHandler: object=" + handlerThread);
            return;
        }
        AwareLog.e(TAG, "setHandler: why handlerThread is null!!");
    }

    public void setRunningPolicy(MemoryScenePolicy memoryScenePolicy) {
        synchronized (mLock) {
            this.mRunningPolicy = memoryScenePolicy;
        }
    }

    public MemoryScenePolicy getRunningPolicy() {
        MemoryScenePolicy memoryScenePolicy;
        synchronized (mLock) {
            memoryScenePolicy = this.mRunningPolicy;
        }
        return memoryScenePolicy;
    }

    public void setPollingPeriod(long pollingPeriod) {
        if (this.mPollingTimer != null) {
            this.mPollingTimer.setPollingPeriod(pollingPeriod);
            this.mPollingTimer.resetTimer();
        }
    }

    public boolean getBigMemAppLaunching() {
        boolean z;
        synchronized (mLock) {
            z = this.mBigMemAppLaunching;
        }
        return z;
    }

    public void setBigMemAppLaunching(boolean isBigMemAppLaunching) {
        synchronized (mLock) {
            this.mBigMemAppLaunching = isBigMemAppLaunching;
        }
    }

    public void setMemoryScenePolicyList(MemoryScenePolicyList memoryScenePolicyList) {
        this.mMemoryScenePolicyList = memoryScenePolicyList;
    }

    public MemoryScenePolicyList getMemoryScenePolicyList() {
        return this.mMemoryScenePolicyList;
    }

    public boolean stopMemoryPolicy(boolean forced) {
        boolean interrupt;
        synchronized (mLock) {
            interrupt = getRunningPolicy() != null ? getRunningPolicy().interrupt(forced) : true;
        }
        return interrupt;
    }

    public void notifyProtectLruState(int state) {
        this.mIdleMemoryExecutor.setProtectCacheFlag(state);
    }
}
