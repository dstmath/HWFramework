package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BigMemoryExecutor extends AbsMemoryExecutor {
    private static final String TAG = "AwareMem_BigMemExec";
    private ThreadPoolExecutor mBigMemAppExecutor;
    private long totalKillMem;
    private long totalRequestMem;

    public BigMemoryExecutor() {
        this.mBigMemAppExecutor = null;
        this.totalRequestMem = 0;
        this.totalKillMem = 0;
        this.mBigMemAppExecutor = new ThreadPoolExecutor(0, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new MemThreadFactory("iaware.mem.bigmem"));
    }

    public void disableMemoryRecover() {
        this.mMemState.setStatus(0);
    }

    public void executeMemoryRecover(Bundle extras) {
        if (extras == null) {
            AwareLog.e(TAG, "executeMemoryRecover extras null");
            return;
        }
        long reqMemory = checkBigAppAvailableMemory(extras);
        if (reqMemory <= 0) {
            this.mMemState.setStatus(0);
            return;
        }
        MemoryScenePolicyList memoryScenePolicyList = MemoryExecutorServer.getInstance().getMemoryScenePolicyList();
        if (memoryScenePolicyList == null) {
            AwareLog.e(TAG, "executeMemoryRecover memoryScenePolicyList null");
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
            this.mBigMemAppExecutor.execute(new CalcRunnable(bigMemoryScenePolicy, true));
        } catch (RejectedExecutionException e) {
            AwareLog.e(TAG, "Failed to execute! reject");
        } catch (Exception e2) {
            AwareLog.e(TAG, "Failed to execute! reset");
        }
    }

    private long checkBigAppAvailableMemory(Bundle extras) {
        long reqMem;
        long targetMemKB;
        if (extras.getInt("event") == 15010) {
            reqMem = extras.getLong("reqMem", 0);
            targetMemKB = reqMem + MemoryConstant.getCriticalMemory();
            this.totalRequestMem += reqMem;
            AwareLog.i(TAG, "request mem event");
        } else {
            targetMemKB = BigMemoryInfo.getInstance().getAppLaunchRequestMemory(extras.getString("appName")) * 1024;
            reqMem = targetMemKB - MemoryConstant.getCriticalMemory();
            this.totalRequestMem = reqMem;
            this.totalKillMem = 0;
            AwareLog.i(TAG, "big mem event " + targetMemKB);
        }
        if (this.totalRequestMem >= 1228800) {
            AwareLog.i(TAG, "total request memory has exceed limit:" + this.totalRequestMem + "KB");
            return -1;
        }
        long availableNow = MemoryReader.getInstance().getMemAvailable();
        long killMem = targetMemKB - availableNow;
        AwareLog.i(TAG, "Avail:" + availableNow + "kb. prepare:" + reqMem + "kb. totalReqMem:" + this.totalRequestMem + "kb");
        if (killMem <= 0) {
            EventTracker.getInstance().trackEvent(1001, 0, 0, "has enough avail mem:" + availableNow);
            return -1;
        }
        this.totalKillMem += killMem;
        AwareLog.i(TAG, "killMem:" + killMem + "kb. totalKill:" + this.totalKillMem + "kb");
        return killMem;
    }
}
