package com.huawei.dalvik.system;

import com.huawei.annotation.HwSystemApi;
import dalvik.system.VMRuntime;

@HwSystemApi
public class VMRuntimeEx {
    public static final int CP_LOAD_IN_MEMORY = 0;
    public static final int CP_NEED_COPY = 1;
    public static final int CP_WRITE_SIGNATURE = 2;

    public static boolean loadAppCyclePattern(String packageName, String processName, String dataDir, long longVersionCode, int useType) {
        VMRuntime.getRuntime();
        return VMRuntime.loadAppCyclePattern(packageName, processName, dataDir, longVersionCode, useType);
    }
}
