package com.huawei.iaware;

import android.os.HandlerThread;

public final class AwareServiceThread extends HandlerThread {
    private static final Object LOCK = new Object();
    private static AwareServiceThread sInstance;

    private AwareServiceThread() {
        super("aware.service", -2);
    }

    private static void init() {
        if (sInstance == null) {
            sInstance = new AwareServiceThread();
            sInstance.start();
        }
    }

    public static AwareServiceThread getInstance() {
        AwareServiceThread awareServiceThread;
        synchronized (LOCK) {
            init();
            awareServiceThread = sInstance;
        }
        return awareServiceThread;
    }
}
