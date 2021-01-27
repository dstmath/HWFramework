package com.android.server.utils;

import android.os.Handler;
import android.os.HandlerThread;

public final class CommonThread extends HandlerThread {
    private static Handler sHandler;
    private static CommonThread sInstance;

    private CommonThread() {
        super("emui.common");
    }

    private static synchronized void ensureThreadLocked() {
        synchronized (CommonThread.class) {
            if (sInstance == null) {
                sInstance = new CommonThread();
                sInstance.start();
                sHandler = new Handler(sInstance.getLooper());
            }
        }
    }

    public static synchronized CommonThread get() {
        CommonThread commonThread;
        synchronized (CommonThread.class) {
            ensureThreadLocked();
            commonThread = sInstance;
        }
        return commonThread;
    }

    public static synchronized Handler getHandler() {
        Handler handler;
        synchronized (CommonThread.class) {
            ensureThreadLocked();
            handler = sHandler;
        }
        return handler;
    }
}
