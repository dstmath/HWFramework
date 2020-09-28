package com.huawei.android.server;

import android.os.HandlerThread;
import android.os.Process;
import android.os.StrictMode;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ServiceThreadEx extends HandlerThread {
    private static final String TAG = "ServiceThreadEx";
    private final boolean mAllowIo;

    public ServiceThreadEx(String name, int priority, boolean allowIo) {
        super(name, priority);
        this.mAllowIo = allowIo;
    }

    public void run() {
        Process.setCanSelfBackground(false);
        if (!this.mAllowIo) {
            StrictMode.initThreadDefaults(null);
        }
        super.run();
    }
}
