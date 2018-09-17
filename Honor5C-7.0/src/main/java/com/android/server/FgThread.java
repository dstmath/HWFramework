package com.android.server;

import android.os.Handler;

public final class FgThread extends ServiceThread {
    private static Handler sHandler;
    private static FgThread sInstance;

    private FgThread() {
        super("android.fg", 0, true);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new FgThread();
            sInstance.start();
            sInstance.getLooper().setTraceTag(64);
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static FgThread get() {
        FgThread fgThread;
        synchronized (UiThread.class) {
            ensureThreadLocked();
            fgThread = sInstance;
        }
        return fgThread;
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
