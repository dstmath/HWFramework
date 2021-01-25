package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.rms.iaware.memory.data.handle.DataInputHandle;
import com.android.server.rms.iaware.memory.utils.CpuReader;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AbsMemoryExecutor {
    private static final int IGNORE_CPULOAD_THRESHOLD = 100;
    private static final String TAG = "AwareMem_AbsMemExec";
    private static long sSysCpuOverLoadCnt = 0;
    AwareMemState mMemState = new AwareMemState();
    AtomicBoolean mStopAction = new AtomicBoolean(false);

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

    public int getProtectCacheFlag() {
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkCpuStatus(MemoryScenePolicy policy, long available, Bundle extras) {
        CpuResult cpuResult = calculateCpuStatus(policy, available);
        extras.putInt("cpuLoad", cpuResult.mCpuLoad);
        extras.putBoolean("cpuBusy", cpuResult.mBusy);
        if (cpuResult.mBusy) {
            EventTracker.getInstance().trackEvent(1001, 0, 0, cpuResult.mTraceInfo);
            sSysCpuOverLoadCnt++;
            return;
        }
        sSysCpuOverLoadCnt = 0;
    }

    private CpuResult calculateCpuStatus(MemoryScenePolicy policy, long mem) {
        String str;
        CpuResult cpuResult = new CpuResult();
        boolean z = true;
        if (!canRecoverExecute("calculateCpuStatus")) {
            cpuResult.mTraceInfo = "DME not running or interrupted";
            cpuResult.mBusy = true;
            return cpuResult;
        } else if (MemoryConstant.getIdleThresHold() >= 100) {
            cpuResult.mTraceInfo = "ignore cpu pressure";
            cpuResult.mBusy = false;
            return cpuResult;
        } else {
            this.mMemState.setStatus(2);
            long cpuLoad = CpuReader.getInstance().getCpuPercent();
            if (cpuLoad < 0) {
                AwareLog.e(TAG, "calculateCpuStatus faild to read cpuload=" + cpuLoad);
                cpuResult.mTraceInfo = "read cpuload err";
                cpuResult.mBusy = true;
                return cpuResult;
            }
            cpuResult.mCpuLoad = (int) cpuLoad;
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "calculateCpuStatus cpu pressure " + cpuLoad);
            }
            if (cpuLoad <= policy.getCpuPressure(mem, MemoryConstant.getIdleThresHold(), sSysCpuOverLoadCnt)) {
                cpuResult.mTraceInfo = "cpuload " + cpuLoad;
                cpuResult.mBusy = false;
                return cpuResult;
            }
            if (cpuLoad <= policy.getCpuPressure(mem, MemoryConstant.getNormalThresHold(), sSysCpuOverLoadCnt)) {
                if (DataInputHandle.getInstance().getActiveStatus() == 2) {
                    z = false;
                }
                cpuResult.mBusy = z;
                if (cpuResult.mBusy) {
                    str = "phone in active state";
                } else {
                    str = "cpuload " + cpuLoad;
                }
                cpuResult.mTraceInfo = str;
            } else {
                cpuResult.mBusy = true;
                cpuResult.mTraceInfo = "cpuload high " + cpuLoad;
            }
            return cpuResult;
        }
    }

    public boolean canRecoverExecute(String functionName) {
        if (!this.mStopAction.get()) {
            return true;
        }
        AwareLog.w(TAG, "function:" + functionName + ", iaware not running, action=" + this.mStopAction.get());
        return false;
    }

    /* access modifiers changed from: private */
    public final class CpuResult {
        boolean mBusy;
        int mCpuLoad;
        String mTraceInfo;

        private CpuResult() {
            this.mCpuLoad = 0;
            this.mBusy = true;
            this.mTraceInfo = null;
        }
    }

    /* access modifiers changed from: protected */
    public static final class AwareMemState {
        static final int CACL_CPU = 2;
        static final int CACL_MEM = 1;
        static final int EXE_ACT = 3;
        static final int IDLE = 0;
        AtomicInteger status = new AtomicInteger(0);

        protected AwareMemState() {
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
            int i = this.status.get();
            if (i == 0) {
                return "status idleing";
            }
            if (i == 1) {
                return "status calculating mem";
            }
            if (i == 2) {
                return "status calculating cpu";
            }
            if (i != 3) {
                return "status error";
            }
            return "status execute action";
        }
    }

    public static final class MemThreadFactory implements ThreadFactory {
        private final String mName;

        public MemThreadFactory(String name) {
            this.mName = name;
        }

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runable) {
            return new Thread(runable, this.mName);
        }
    }

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

        @Override // java.lang.Runnable
        public void run() {
            boolean rtSchedSet = CleanSource.setSchedPriority();
            Bundle extras = this.mMemoryScenePolicy.getExtras();
            if (AbsMemoryExecutor.this.mStopAction.get() || extras == null) {
                this.mMemoryScenePolicy.clear();
                if (this.mLaunchBigMemApp) {
                    MemoryExecutorServer.getInstance().setBigMemAppLaunching(false);
                } else {
                    MemoryExecutorServer.getInstance().setRunningPolicy(null);
                }
                CleanSource.resetSchedPriority(rtSchedSet);
                return;
            }
            if (!extras.getBoolean("emergency") && !this.mLaunchBigMemApp) {
                AbsMemoryExecutor.this.checkCpuStatus(this.mMemoryScenePolicy, extras.getLong("available"), extras);
                if (extras.getBoolean("cpuBusy")) {
                    CleanSource.resetSchedPriority(rtSchedSet);
                    return;
                }
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
            EventTracker.getInstance().trackEvent(EventTracker.TRACK_TYPE_END, 0, 0, null);
            MemoryExecutorServer.getInstance().setPollingPeriod(this.mMemoryScenePolicy.getPollingPeriod());
            AbsMemoryExecutor.this.mMemState.setStatus(0);
            CleanSource.resetSchedPriority(rtSchedSet);
        }
    }
}
