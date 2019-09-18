package com.android.server;

import android.common.HwFrameworkFactory;
import android.os.Handler;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;

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
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("appeye_frameworkblock");
            if (iZrHung != null) {
                ZrHungData data = new ZrHungData();
                data.put("thread", sHandler);
                iZrHung.check(data);
            }
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
