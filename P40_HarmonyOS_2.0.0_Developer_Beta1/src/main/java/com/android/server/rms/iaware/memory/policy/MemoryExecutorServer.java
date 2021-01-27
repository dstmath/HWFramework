package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.PollingTimer;
import com.huawei.android.os.SystemPropertiesEx;

public class MemoryExecutorServer {
    private static final Object LOCK = new Object();
    private static final String TAG = "AwareMem_MemExecSvr";
    private static MemoryExecutorServer sMemoryExecutorServer;
    private final AbsMemoryExecutor mBigMemoryExecutor = new BigMemoryExecutor();
    private boolean mFirstBooting = true;
    FirstBootingHandler mFirstBootingHandler = null;
    private final AbsMemoryExecutor mIdleMemoryExecutor = new IdleMemoryExecutor();
    boolean mIsBigMemAppLaunching = false;
    MemoryScenePolicyList mMemoryScenePolicyList = null;
    PollingTimer mPollingTimer = new PollingTimer();
    MemoryScenePolicy mRunningPolicy = null;

    private MemoryExecutorServer() {
    }

    public static MemoryExecutorServer getInstance() {
        MemoryExecutorServer memoryExecutorServer;
        synchronized (LOCK) {
            if (sMemoryExecutorServer == null) {
                sMemoryExecutorServer = new MemoryExecutorServer();
            }
            memoryExecutorServer = sMemoryExecutorServer;
        }
        return memoryExecutorServer;
    }

    public void enable() {
        this.mIdleMemoryExecutor.enableMemoryRecover();
        this.mBigMemoryExecutor.enableMemoryRecover();
        setPollingPeriod(MemoryConstant.getEnableTimerPeriod());
        this.mMemoryScenePolicyList.clear();
        if (this.mFirstBootingHandler == null) {
            this.mFirstBootingHandler = new FirstBootingHandler();
            this.mFirstBootingHandler.start();
        }
        AwareLog.i(TAG, "enable");
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
            AwareLog.w(TAG, "executeMemoryRecover null policy!!");
        } else {
            extras.putInt("event", event);
            extras.putLong("timeStamp", timeStamp);
            if (MemoryConstant.MEM_SCENE_BIGMEM.equals(scene)) {
                this.mBigMemoryExecutor.executeMemoryRecover(extras);
            } else if (isFirstBooting()) {
                AwareLog.i(TAG, "executeMemoryRecover booting and delay!");
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
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "setHandler: object=" + handlerThread);
                return;
            }
            return;
        }
        AwareLog.w(TAG, "setHandler: why handlerThread is null!!");
    }

    public void setRunningPolicy(MemoryScenePolicy memoryScenePolicy) {
        synchronized (LOCK) {
            this.mRunningPolicy = memoryScenePolicy;
        }
    }

    public MemoryScenePolicy getRunningPolicy() {
        MemoryScenePolicy memoryScenePolicy;
        synchronized (LOCK) {
            memoryScenePolicy = this.mRunningPolicy;
        }
        return memoryScenePolicy;
    }

    public void setPollingPeriod(long pollingPeriod) {
        PollingTimer pollingTimer = this.mPollingTimer;
        if (pollingTimer != null) {
            pollingTimer.setPollingPeriod(pollingPeriod);
            this.mPollingTimer.resetTimer();
        }
    }

    public boolean getBigMemAppLaunching() {
        boolean z;
        synchronized (LOCK) {
            z = this.mIsBigMemAppLaunching;
        }
        return z;
    }

    public void setBigMemAppLaunching(boolean isBigMemAppLaunching) {
        synchronized (LOCK) {
            this.mIsBigMemAppLaunching = isBigMemAppLaunching;
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
        synchronized (LOCK) {
            interrupt = getRunningPolicy() != null ? getRunningPolicy().interrupt(forced) : true;
        }
        return interrupt;
    }

    public void notifyProtectLruState(int state) {
        this.mIdleMemoryExecutor.setProtectCacheFlag(state);
    }

    public int getProtectLruState() {
        return this.mIdleMemoryExecutor.getProtectCacheFlag();
    }

    public boolean isFirstBooting() {
        return MemoryConstant.isCleanAllSwitch() && this.mFirstBooting;
    }

    public void firstBootingFinish() {
        this.mFirstBootingHandler.stop();
        AwareLog.i(TAG, "firstBootingFinish=" + this.mFirstBooting);
    }

    /* access modifiers changed from: private */
    public final class FirstBootingHandler extends Handler {
        private static final long MAX_DELAY = 120000;
        private static final long MIN_DELAY = 60000;
        private static final int MSG_BOOT_COMPLETED = 100;
        private static final int MSG_BOOT_WAITING = 101;
        private static final long STEP_DELAY = 15000;
        private long mDoing;

        private FirstBootingHandler() {
            this.mDoing = 0;
        }

        public void start() {
            AwareLog.i(MemoryExecutorServer.TAG, "start firstBoot=" + MemoryConstant.isCleanAllSwitch());
            if (MemoryConstant.isCleanAllSwitch()) {
                this.mDoing = SystemClock.elapsedRealtime();
                if (isBootCompleted()) {
                    sendEmptyMessageDelayed(100, MAX_DELAY);
                } else {
                    sendEmptyMessageDelayed(101, STEP_DELAY);
                }
            } else {
                sendEmptyMessageDelayed(100, STEP_DELAY);
            }
        }

        public void stop() {
            AwareLog.i(MemoryExecutorServer.TAG, "stop firstBoot=" + MemoryConstant.isCleanAllSwitch());
            if (MemoryConstant.isCleanAllSwitch()) {
                removeMessages(100);
                long interval = SystemClock.elapsedRealtime() - this.mDoing;
                if (interval < 60000) {
                    sendEmptyMessageDelayed(100, 60000 - interval);
                    return;
                }
            }
            MemoryExecutorServer.this.mFirstBooting = false;
        }

        private boolean isBootCompleted() {
            return "1".equals(SystemPropertiesEx.get("sys.boot_completed", "0"));
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 100) {
                stop();
                AwareLog.i(MemoryExecutorServer.TAG, "BOOT_COMPLETED=" + MemoryExecutorServer.this.mFirstBooting);
            } else if (i == 101) {
                if (!isBootCompleted()) {
                    sendEmptyMessageDelayed(101, STEP_DELAY);
                } else {
                    this.mDoing = SystemClock.elapsedRealtime();
                    sendEmptyMessageDelayed(100, MAX_DELAY);
                }
                AwareLog.i(MemoryExecutorServer.TAG, "BOOT_WAITING=" + MemoryExecutorServer.this.mFirstBooting);
            }
        }
    }
}
