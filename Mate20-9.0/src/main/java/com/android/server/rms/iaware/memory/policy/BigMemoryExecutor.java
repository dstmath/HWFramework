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
    private static final String TAG = "AwareMem_BigMemExec";
    private ThreadPoolExecutor mBigMemAppExecutor = null;
    private long totalKillMem = 0;

    public BigMemoryExecutor() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new AbsMemoryExecutor.MemThreadFactory("iaware.mem.bigmem"));
        this.mBigMemAppExecutor = threadPoolExecutor;
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
            this.mBigMemAppExecutor.execute(new AbsMemoryExecutor.CalcRunnable(bigMemoryScenePolicy, true));
        } catch (RejectedExecutionException e) {
            AwareLog.e(TAG, "Failed to execute! reject");
        } catch (Exception e2) {
            AwareLog.e(TAG, "Failed to execute! reset");
        }
    }

    private long checkBigAppAvailableMemory(Bundle extras) {
        long targetMemKB;
        long reqMem;
        Bundle bundle = extras;
        int event = bundle.getInt("event");
        if (event == 15010) {
            reqMem = bundle.getLong("reqMem", 0);
            MemoryConstant.addTotalAPIRequestMemory(reqMem);
            AwareLog.i(TAG, "request mem event");
            targetMemKB = MemoryConstant.getCriticalMemory() + reqMem;
        } else {
            targetMemKB = BigMemoryInfo.getInstance().getAppLaunchRequestMemory(bundle.getString("appName")) * 1024;
            reqMem = targetMemKB - MemoryConstant.getCriticalMemory();
            MemoryConstant.addTotalAPIRequestMemory(reqMem);
            this.totalKillMem = 0;
            AwareLog.i(TAG, "big mem event target" + targetMemKB + "kb");
        }
        if (MemoryConstant.getTotalAPIRequestMemory() > MemoryConstant.getMaxAPIRequestMemory()) {
            AwareLog.i(TAG, "total request memory has exceed limit:" + totalRequestMem + "kb");
            return -1;
        }
        long killMem = targetMemKB - MemoryReader.getInstance().getMemAvailable();
        AwareLog.i(TAG, "Avail:" + availableNow + "kb. prepare:" + reqMem + "kb. totalReqMem:" + totalRequestMem + "kb");
        if (killMem <= 0) {
            EventTracker.getInstance().trackEvent(1001, 0, 0, "has enough avail mem:" + availableNow);
            return -1;
        }
        this.totalKillMem += killMem;
        StringBuilder sb = new StringBuilder();
        sb.append("killMem:");
        sb.append(killMem);
        sb.append("kb. totalKill:");
        int i = event;
        sb.append(this.totalKillMem);
        sb.append("kb");
        AwareLog.i(TAG, sb.toString());
        return killMem;
    }
}
