package com.android.server.wm;

import android.os.Handler;
import com.android.server.ServiceThread;

public final class SurfaceAnimationThread extends ServiceThread {
    private static Handler sHandler;
    private static SurfaceAnimationThread sInstance;

    private SurfaceAnimationThread() {
        super("android.anim.lf", -4, false);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new SurfaceAnimationThread();
            sInstance.start();
            sInstance.getLooper().setTraceTag(32);
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static SurfaceAnimationThread get() {
        SurfaceAnimationThread surfaceAnimationThread;
        synchronized (SurfaceAnimationThread.class) {
            ensureThreadLocked();
            surfaceAnimationThread = sInstance;
        }
        return surfaceAnimationThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (SurfaceAnimationThread.class) {
            ensureThreadLocked();
            handler = sHandler;
        }
        return handler;
    }
}
