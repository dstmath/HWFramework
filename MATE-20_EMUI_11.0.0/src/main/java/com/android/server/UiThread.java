package com.android.server;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

public final class UiThread extends ServiceThread {
    private static final long SLOW_DELIVERY_THRESHOLD_MS = 200;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 100;
    private static Handler sHandler;
    private static UiThread sInstance;

    private UiThread() {
        super("android.ui", -2, false);
    }

    @Override // com.android.server.ServiceThread, android.os.HandlerThread, java.lang.Thread, java.lang.Runnable
    public void run() {
        Process.setThreadGroup(Process.myTid(), 5);
        super.run();
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new UiThread();
            sInstance.start();
            Looper looper = sInstance.getLooper();
            looper.setTraceTag(524288);
            looper.setSlowLogThresholdMs(SLOW_DISPATCH_THRESHOLD_MS, SLOW_DELIVERY_THRESHOLD_MS);
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static UiThread get() {
        UiThread uiThread;
        synchronized (UiThread.class) {
            ensureThreadLocked();
            uiThread = sInstance;
        }
        return uiThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (UiThread.class) {
            ensureThreadLocked();
            handler = sHandler;
        }
        return handler;
    }
}
