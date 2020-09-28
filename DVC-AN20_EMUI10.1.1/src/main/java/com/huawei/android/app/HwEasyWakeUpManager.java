package com.huawei.android.app;

import com.huawei.android.os.ServiceManagerEx;
import huawei.android.app.IEasyWakeUpManager;

public class HwEasyWakeUpManager {
    public static boolean setEasyWakeUpFlag(int value) {
        try {
            IEasyWakeUpManager.Stub.asInterface(ServiceManagerEx.getService("easywakeup")).setEasyWakeUpFlag(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
