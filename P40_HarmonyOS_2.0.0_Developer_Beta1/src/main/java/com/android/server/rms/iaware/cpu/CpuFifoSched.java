package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.huawei.android.os.ProcessExt;
import java.util.Map;

public class CpuFifoSched {
    private static final Object SLOCK = new Object();
    private static final String TAG = "CpuFifoSched";
    private static CpuFifoSched sInstance;
    private Map<String, Integer> mFifoSchedConfig = new ArrayMap();
    private Map<Integer, Integer> mFifoSchedThread = new ArrayMap();

    private CpuFifoSched() {
    }

    public static CpuFifoSched getInstance() {
        CpuFifoSched cpuFifoSched;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuFifoSched();
            }
            cpuFifoSched = sInstance;
        }
        return cpuFifoSched;
    }

    public void setFifoSchedConfig(Map<String, String> config) {
        if (config == null) {
            AwareLog.w(TAG, "setFifoSchedConifg config is null.");
            return;
        }
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String tidName = entry.getKey();
            String value = entry.getValue();
            if (!(tidName == null || value == null)) {
                int priority = CpuCommonUtil.parseInt(value);
                if (priority == 0 || priority == 1) {
                    this.mFifoSchedConfig.put(tidName, Integer.valueOf(priority));
                } else {
                    AwareLog.w(TAG, "setFifoSchedConifg: priority is invalid, priority = " + priority);
                }
            }
        }
        AwareLog.i(TAG, "setFifoSchedConifg mFifoSchedConfig = " + this.mFifoSchedConfig);
    }

    public void start(CpuFeature feature) {
        setThreadFifoSched(true);
    }

    public void stop() {
        resetFifoSched();
    }

    private void resetFifoSched() {
        setThreadFifoSched(false);
        this.mFifoSchedThread.clear();
        this.mFifoSchedConfig.clear();
    }

    /* access modifiers changed from: protected */
    public Map<String, Integer> getFifoConfig() {
        return this.mFifoSchedConfig;
    }

    /* access modifiers changed from: protected */
    public void setFifoSchedThread(String threadName, String tidPath) {
        Integer value;
        int tid;
        if (threadName != null && tidPath != null && (value = this.mFifoSchedConfig.get(threadName)) != null && (tid = CpuCommonUtil.getThreadId(tidPath)) > 0) {
            this.mFifoSchedThread.put(Integer.valueOf(tid), value);
        }
    }

    private void setThreadFifoSched(boolean isSet) {
        if (!this.mFifoSchedThread.isEmpty()) {
            AwareLog.i(TAG, "setThreadFifoSched mFifoSchedThread = " + this.mFifoSchedThread);
            try {
                for (Map.Entry<Integer, Integer> entry : this.mFifoSchedThread.entrySet()) {
                    Integer tid = entry.getKey();
                    Integer value = entry.getValue();
                    if (tid != null) {
                        if (value != null) {
                            if (isSet) {
                                ProcessExt.setThreadScheduler(tid.intValue(), 1073741825, value.intValue());
                            } else {
                                ProcessExt.setThreadScheduler(tid.intValue(), 0, value.intValue());
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                AwareLog.w(TAG, "Failed to set scheduling policy, thread does not exist:\n" + e);
            } catch (SecurityException e2) {
                AwareLog.w(TAG, "Failed to set scheduling policy, not allowed:\n" + e2);
            }
        }
    }
}
