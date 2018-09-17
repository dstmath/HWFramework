package com.android.server.emcom;

import android.os.HandlerThread;
import android.os.Looper;

public final class EmcomThread extends HandlerThread {
    private static EmcomThread sInstance;

    private EmcomThread() {
        super("EmcomThread");
    }

    public static synchronized EmcomThread getInstance() {
        EmcomThread emcomThread;
        synchronized (EmcomThread.class) {
            if (sInstance == null) {
                sInstance = new EmcomThread();
                sInstance.start();
            }
            emcomThread = sInstance;
        }
        return emcomThread;
    }

    public static Looper getInstanceLooper() {
        return getInstance().getLooper();
    }
}
