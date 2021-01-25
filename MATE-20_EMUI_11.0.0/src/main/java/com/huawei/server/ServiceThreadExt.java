package com.huawei.server;

import android.os.HandlerThread;
import android.os.Looper;
import com.android.server.ServiceThread;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ServiceThreadExt {
    private ServiceThread mServiceThread;

    public ServiceThreadExt(String tag, int priority, boolean allowIo) {
        this.mServiceThread = new ServiceThread(tag, priority, allowIo);
    }

    public void start() {
        this.mServiceThread.start();
    }

    public Looper getLooper() {
        return this.mServiceThread.getLooper();
    }

    public HandlerThread getHandlerThread() {
        return this.mServiceThread;
    }
}
