package com.huawei.android.server;

import android.os.HandlerThread;
import android.os.Looper;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class FgThreadEx {
    private static final long SLOW_DELIVERY_THRESHOLD_MS = 200;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 100;

    private FgThreadEx() {
    }

    public static HandlerThread createFgThread(String name) {
        HandlerThread thread = new ServiceThreadEx(name, 0, true);
        thread.start();
        Looper looper = thread.getLooper();
        looper.setTraceTag(524288);
        looper.setSlowLogThresholdMs(SLOW_DISPATCH_THRESHOLD_MS, SLOW_DELIVERY_THRESHOLD_MS);
        return thread;
    }
}
