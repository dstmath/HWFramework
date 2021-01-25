package com.huawei.android.os;

import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HandlerThreadEx {
    public static Handler getThreadHandler(HandlerThread handlerThread) {
        return handlerThread.getThreadHandler();
    }

    public static void start(HandlerThread handlerThread) {
        handlerThread.start();
    }
}
