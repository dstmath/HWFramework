package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AbsMemoryExecutor {
    private static final String TAG = "AwareMem_AbsMemExec";
    IAwareMemState mMemState = new IAwareMemState();
    AtomicBoolean mStopAction = new AtomicBoolean(false);

    protected final class CalcRunnable implements Runnable {
        private final boolean mLaunchBigMemApp;
        private final MemoryScenePolicy mMemoryScenePolicy;

        CalcRunnable(MemoryScenePolicy memoryScenePolicy, boolean launchBigMemApp) {
            this.mMemoryScenePolicy = memoryScenePolicy;
            this.mLaunchBigMemApp = launchBigMemApp;
            if (this.mLaunchBigMemApp) {
                MemoryExecutorServer.getInstance().setBigMemAppLaunching(true);
            } else {
                MemoryExecutorServer.getInstance().setRunningPolicy(memoryScenePolicy);
            }
        }

        public void run() {
            if (AbsMemoryExecutor.this.mStopAction.get()) {
                this.mMemoryScenePolicy.clear();
                if (this.mLaunchBigMemApp) {
                    MemoryExecutorServer.getInstance().setBigMemAppLaunching(false);
                } else {
                    MemoryExecutorServer.getInstance().setRunningPolicy(null);
                }
                return;
            }
            Process.setThreadPriority(this.mLaunchBigMemApp ? -2 : 10);
            AbsMemoryExecutor.this.mMemState.setStatus(3);
            this.mMemoryScenePolicy.execute();
            this.mMemoryScenePolicy.clear();
            if (this.mLaunchBigMemApp) {
                MemoryExecutorServer.getInstance().setBigMemAppLaunching(false);
            } else {
                MemoryExecutorServer.getInstance().setRunningPolicy(null);
            }
            EventTracker.getInstance().trackEvent(1004, 0, 0, null);
            MemoryExecutorServer.getInstance().setPollingPeriod(this.mMemoryScenePolicy.getPollingPeriod());
            AbsMemoryExecutor.this.mMemState.setStatus(0);
        }
    }

    protected static final class IAwareMemState {
        static final int CACL_CPU = 2;
        static final int CACL_MEM = 1;
        static final int EXE_ACT = 3;
        static final int IDLE = 0;
        AtomicInteger status = new AtomicInteger(0);

        protected IAwareMemState() {
        }

        public void reset() {
            this.status.getAndSet(0);
        }

        public void setStatus(int stat) {
            this.status.getAndSet(stat);
        }

        public int getStatus() {
            return this.status.get();
        }

        public String toString() {
            switch (this.status.get()) {
                case 0:
                    return "status idleing";
                case 1:
                    return "status calculating mem";
                case 2:
                    return "status calculating cpu";
                case 3:
                    return "status execute action";
                default:
                    return "status error";
            }
        }
    }

    protected static final class MemThreadFactory implements ThreadFactory {
        private final String mName;

        MemThreadFactory(String name) {
            this.mName = name;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, this.mName);
        }
    }

    public void enableMemoryRecover() {
        this.mMemState.setStatus(0);
    }

    public void disableMemoryRecover() {
    }

    public void stopMemoryRecover() {
    }

    public void executeMemoryRecover(Bundle extras) {
    }

    public void setMemHandlerThread(HandlerThread handlerThread) {
    }

    public void setProtectCacheFlag(int state) {
    }
}
