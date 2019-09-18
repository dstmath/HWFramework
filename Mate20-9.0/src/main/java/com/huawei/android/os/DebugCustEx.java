package com.huawei.android.os;

public class DebugCustEx {
    private static final native String native_getAptempInfo();

    private static final native String native_getCpuInfo();

    private static final native String native_getCurrentInfo();

    private static final native String native_getDdrInfo();

    private static final native String native_getGpuInfo();

    private static final native float native_getSurfaceFlingerFrameRate();

    static {
        System.loadLibrary("debugcustex_jni");
    }

    public static final float getSurfaceFlingerFrameRate() {
        return native_getSurfaceFlingerFrameRate();
    }

    public static final String getCpuInfo() {
        return native_getCpuInfo();
    }

    public static final String getGpuInfo() {
        return native_getGpuInfo();
    }

    public static final String getDdrInfo() {
        return native_getDdrInfo();
    }

    public static final String getCurrentInfo() {
        return native_getCurrentInfo();
    }

    public static final String getAptempInfo() {
        return native_getAptempInfo();
    }
}
