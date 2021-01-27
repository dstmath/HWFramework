package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BigMemoryExecutor extends AbsMemoryExecutor {
    private static final long MEMORY_KB_FACTOR = 1024;
    private static final String TAG = "AwareMem_BigMemExec";
    private static final long THREAD_POOL_ALIVE_TIME = 10;
    private ThreadPoolExecutor mBigMemAppExecutor;

    public BigMemoryExecutor() {
        this.mBigMemAppExecutor = null;
        this.mBigMemAppExecutor = new ThreadPoolExecutor(0, 1, (long) THREAD_POOL_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new AbsMemoryExecutor.MemThreadFactory("iaware.mem.bigmem"));
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public void disableMemoryRecover() {
        this.mMemState.setStatus(0);
    }

    @Override // com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor
    public void executeMemoryRecover(Bundle extras) {
        if (extras == null) {
            AwareLog.w(TAG, "executeMemoryRecover extras null");
            return;
        }
        long reqMemory = checkBigAppAvailableMemory(extras);
        if (reqMemory <= 0) {
            this.mMemState.setStatus(0);
            return;
        }
        MemoryScenePolicyList memoryScenePolicyList = MemoryExecutorServer.getInstance().getMemoryScenePolicyList();
        if (memoryScenePolicyList == null) {
            AwareLog.w(TAG, "executeMemoryRecover memoryScenePolicyList null");
            return;
        }
        MemoryScenePolicy bigMemoryScenePolicy = memoryScenePolicyList.getMemoryScenePolicy(MemoryConstant.MEM_SCENE_BIGMEM);
        if (bigMemoryScenePolicy == null) {
            this.mMemState.setStatus(0);
            return;
        }
        extras.putLong("reqMem", reqMemory);
        bigMemoryScenePolicy.setExtras(extras);
        MemoryExecutorServer.getInstance().stopMemoryPolicy(true);
        try {
            this.mBigMemAppExecutor.execute(new AbsMemoryExecutor.CalcRunnable(bigMemoryScenePolicy, true));
        } catch (RejectedExecutionException e) {
            AwareLog.e(TAG, "Failed to execute! reject");
        } catch (Exception e2) {
            AwareLog.e(TAG, "Failed to execute! reset");
        }
    }

    private long checkBigAppAvailableMemory(Bundle extras) {
        long targetMemKb;
        if (extras == null) {
            AwareLog.w(TAG, "checkBigAppAvailableMemory extras is null");
            return -1;
        }
        if (extras.getInt("event") == 15010) {
            targetMemKb = MemoryConstant.getCriticalMemory() + extras.getLong("reqMem", 0);
            AwareLog.i(TAG, "request mem event");
        } else {
            String appName = extras.getString("appName");
            targetMemKb = BigMemoryInfo.getInstance().getAppLaunchRequestMemory(appName) * MEMORY_KB_FACTOR;
            if (BigMemoryInfo.getInstance().isBigMemFeatureOn()) {
                int uid = extras.getInt("appUid");
                if (uid <= 0) {
                    AwareLog.w(TAG, "extras does not set app uid");
                    return -1;
                }
                targetMemKb = BigMemoryInfo.getInstance().getAppLaunchRequestMemory(uid, appName);
            }
            AwareLog.i(TAG, "big mem event target" + targetMemKb + "kb");
        }
        long availableNow = MemoryReader.getInstance().getMemAvailable();
        long killMem = targetMemKb - availableNow;
        if (killMem <= 0) {
            AwareLog.i(TAG, "enough mem, not kill");
            EventTracker instance = EventTracker.getInstance();
            instance.trackEvent(1001, 0, 0, "has enough avail mem:" + availableNow);
            return -1;
        }
        MemoryConstant.addTotalRequestMemory(killMem);
        long totalKillMem = MemoryConstant.getTotalRequestMemory();
        if (totalKillMem > MemoryConstant.getMaxRequestMemory()) {
            AwareLog.i(TAG, "total kill memory has exceed limit:" + totalKillMem + "kb");
            return -1;
        }
        AwareLog.i(TAG, "killMem:" + killMem + "kb. totalKill:" + totalKillMem + "kb");
        return killMem;
    }
}
