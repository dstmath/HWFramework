package com.huawei.android.deviceinfo;

import huawei.android.deviceinfo.HwDeviceInfoManager;

public class HwDeviceInfoEx {
    public static String getDeviceInfo(int type) {
        return HwDeviceInfoManager.getInstance().getDeviceInfo(type);
    }
}
