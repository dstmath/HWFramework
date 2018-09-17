package com.android.server;

import android.os.Handler;

public final class UiThread extends ServiceThread {
    private static Handler sHandler;
    private static UiThread sInstance;

    private UiThread() {
        super("android.ui", -2, false);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new UiThread();
            sInstance.start();
            sInstance.getLooper().setTraceTag(64);
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
