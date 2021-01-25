package com.android.internal.os;

import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.Looper;
import java.util.concurrent.Executor;

public final class BackgroundThread extends HandlerThread {
    private static final long SLOW_DELIVERY_THRESHOLD_MS = 30000;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 10000;
    private static Handler sHandler;
    private static HandlerExecutor sHandlerExecutor;
    private static BackgroundThread sInstance;

    private BackgroundThread() {
        super("android.bg", 10);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new BackgroundThread();
            sInstance.start();
            Looper looper = sInstance.getLooper();
            looper.setTraceTag(524288);
            looper.setSlowLogThresholdMs(10000, 30000);
            sHandler = new Handler(sInstance.getLooper());
            sHandlerExecutor = new HandlerExecutor(sHandler);
        }
    }

    public static BackgroundThread get() {
        BackgroundThread backgroundThread;
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            backgroundThread = sInstance;
        }
        return backgroundThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            handler = sHandler;
        }
        return handler;
    }

    public static Executor getExecutor() {
        HandlerExecutor handlerExecutor;
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            handlerExecutor = sHandlerExecutor;
        }
        return handlerExecutor;
    }
}
