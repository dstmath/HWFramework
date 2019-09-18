package com.android.server;

import android.os.Handler;

public final class IoThread extends ServiceThread {
    private static Handler sHandler;
    private static IoThread sInstance;

    private IoThread() {
        super("android.io", 0, true);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new IoThread();
            sInstance.start();
            sInstance.getLooper().setTraceTag(524288);
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static IoThread get() {
        IoThread ioThread;
        synchronized (IoThread.class) {
            ensureThreadLocked();
            ioThread = sInstance;
        }
        return ioThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (IoThread.class) {
            ensureThreadLocked();
            handler = sHandler;
        }
        return handler;
    }
}
