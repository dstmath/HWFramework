package com.android.server;

import android.os.Handler;

public final class DisplayThread extends ServiceThread {
    private static Handler sHandler;
    private static DisplayThread sInstance;

    private DisplayThread() {
        super("android.display", -3, false);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new DisplayThread();
            sInstance.start();
            sInstance.getLooper().setTraceTag(524288);
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static DisplayThread get() {
        DisplayThread displayThread;
        synchronized (DisplayThread.class) {
            ensureThreadLocked();
            displayThread = sInstance;
        }
        return displayThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (DisplayThread.class) {
            ensureThreadLocked();
            handler = sHandler;
        }
        return handler;
    }
}
