package com.huawei.server;

import android.os.Looper;
import com.android.server.DisplayThread;

public class DisplayThreadEx {
    public static Looper getLooper() {
        return DisplayThread.get().getLooper();
    }
}
