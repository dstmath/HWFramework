package com.huawei.internal.os;

import android.os.Handler;
import android.os.Looper;
import com.android.internal.os.BackgroundThread;

public final class BackgroundThreadEx {
    private BackgroundThreadEx() {
    }

    public static Handler getHandler() {
        return BackgroundThread.getHandler();
    }

    public static Looper getLooper() {
        return BackgroundThread.get().getLooper();
    }
}
