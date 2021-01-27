package com.huawei.android.app;

import android.app.IApplicationThread;
import android.os.IBinder;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IApplicationThreadEx {
    private IApplicationThread mApplicationThread;

    public IApplicationThreadEx(IApplicationThread thread) {
        this.mApplicationThread = thread;
    }

    public IBinder getBinder() {
        return this.mApplicationThread.asBinder();
    }

    public boolean isApplicationThreadNull() {
        return this.mApplicationThread == null;
    }
}
