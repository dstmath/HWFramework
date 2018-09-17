package com.huawei.android.util;

public class SystemInfo {
    public static String getDeviceRam() {
        return HwSystemInfo.getDeviceRam();
    }

    public static String getDeviceEmmc() {
        return HwSystemInfo.getDeviceEmmc();
    }
}
