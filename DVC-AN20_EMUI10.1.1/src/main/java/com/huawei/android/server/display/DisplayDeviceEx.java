package com.huawei.android.server.display;

import huawei.android.os.HwGeneralManager;

public class DisplayDeviceEx {
    public static boolean isCurveScreen() {
        return HwGeneralManager.getInstance().isCurveScreen();
    }
}
