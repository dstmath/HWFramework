package com.huawei.server.rme.hyperhold;

import android.os.HandlerThread;

public final class HyperHoldServiceThread extends HandlerThread {
    private static final Object LOCK = new Object();
    private static HyperHoldServiceThread hyperholdInstance;

    private HyperHoldServiceThread() {
        super("hyperhold.service", -2);
    }

    private static void init() {
        if (hyperholdInstance == null) {
            hyperholdInstance = new HyperHoldServiceThread();
            hyperholdInstance.start();
        }
    }

    public static HyperHoldServiceThread getInstance() {
        HyperHoldServiceThread hyperHoldServiceThread;
        synchronized (LOCK) {
            init();
            hyperHoldServiceThread = hyperholdInstance;
        }
        return hyperHoldServiceThread;
    }
}
