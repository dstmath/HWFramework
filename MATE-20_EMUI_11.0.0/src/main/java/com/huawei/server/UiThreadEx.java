package com.huawei.server;

import android.os.Handler;
import android.os.Looper;
import com.android.server.UiThread;

public class UiThreadEx {
    public static Handler getHandler() {
        return UiThread.getHandler();
    }

    public static Looper getLooper() {
        return UiThread.get().getLooper();
    }
}
