package com.huawei.android.os;

import huawei.android.os.HwDeviceIdleManager;

public class DeviceidleEx {
    public static int forceIdle() {
        return HwDeviceIdleManager.getInstance().forceIdle();
    }
}
