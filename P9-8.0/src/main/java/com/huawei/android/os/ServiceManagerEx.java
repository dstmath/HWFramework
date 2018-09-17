package com.huawei.android.os;

import android.os.IBinder;
import android.os.ServiceManager;

public final class ServiceManagerEx {
    public static IBinder getService(String name) {
        return ServiceManager.getService(name);
    }

    public static IBinder checkService(String name) {
        return ServiceManager.checkService(name);
    }
}
