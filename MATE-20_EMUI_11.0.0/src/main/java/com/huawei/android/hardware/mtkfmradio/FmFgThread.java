package com.huawei.android.hardware.mtkfmradio;

import android.os.HandlerThread;
import android.os.Looper;
import com.huawei.android.server.FgThreadEx;

/* access modifiers changed from: package-private */
public class FmFgThread {
    private static HandlerThread sInstance;

    private FmFgThread() {
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = FgThreadEx.createFgThread("FmService");
        }
    }

    public static Looper getLooper() {
        Looper looper;
        synchronized (FmFgThread.class) {
            ensureThreadLocked();
            looper = sInstance.getLooper();
        }
        return looper;
    }
}
