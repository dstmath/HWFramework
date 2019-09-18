package com.huawei.demo;

import android.os.SystemProperties;

public class HwDemoUtils {
    public static boolean isDemoVersion() {
        return "demo".equalsIgnoreCase(SystemProperties.get("ro.hw.vendor", "")) || "demo".equalsIgnoreCase(SystemProperties.get("ro.hw.country", ""));
    }
}
