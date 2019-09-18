package com.huawei.android.app;

import android.os.ServiceManager;
import huawei.android.app.IEasyWakeUpManager;

public class HwEasyWakeUpManager {
    public static boolean setEasyWakeUpFlag(int value) {
        try {
            IEasyWakeUpManager.Stub.asInterface(ServiceManager.getService("easywakeup")).setEasyWakeUpFlag(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
