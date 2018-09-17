package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.security.trustcircle.IOTController;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BigMemoryExecutor extends AbsMemoryExecutor {
    private static final String TAG = "AwareMem_BigMemExec";
    private ThreadPoolExecutor mBigMemAppExecutor;

    public BigMemoryExecutor() {
        this.mBigMemAppExecutor = null;
        this.mBigMemAppExecutor = new ThreadPoolExecutor(0, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new MemThreadFactory("iaware.mem.bigmem"));
    }

    public void disableMemoryRecover() {
        this.mMemState.setStatus(0);
    }

    public void executeMemoryRecover(Bundle extras) {
        if (extras == null) {
            AwareLog.e(TAG, "createPolicyByMemCpu extras null");
            return;
        }
        long reqMemory = checkBigAppAvailableMemory(extras);
        if (reqMemory <= 0) {
            this.mMemState.setStatus(0);
            return;
        }
        MemoryScenePolicyList memoryScenePolicyList = MemoryExecutorServer.getInstance().getMemoryScenePolicyList();
        if (memoryScenePolicyList == null) {
            AwareLog.e(TAG, "createPolicyByMemCpu memoryScenePolicyList null");
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
        String appName = extras.getString("appName");
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        long bigAppRequestMem = BigMemoryInfo.getInstance().getAppLaunchRequestMemory(appName);
        long bigAppRequestMemKB = bigAppRequestMem * 1024;
        long reqMemory = bigAppRequestMemKB - availableRam;
        if (availableRam >= bigAppRequestMemKB) {
            EventTracker.getInstance().trackEvent(IOTController.TYPE_SLAVE, 0, 0, "system has enough availableRam:" + availableRam);
            return -1;
        }
        AwareLog.d(TAG, "Big Mem App execMemAction() Directly! current Mem:" + availableRam + " app=" + appName + " request memory is " + bigAppRequestMem + "MB");
        return reqMemory;
    }
}
