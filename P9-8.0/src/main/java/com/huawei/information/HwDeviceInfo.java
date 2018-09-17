package com.huawei.information;

import android.util.Log;

public class HwDeviceInfo {
    private static final String TAG = "HwDeviceInfo";

    private static native String get_emmc_id();

    public static String getEMMCID() {
        try {
            Log.d(TAG, "HwDeviceInfo 64bits so, getEMMCID");
            return get_emmc_id();
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "libarary hwdeviceinfo get_emmc_id failed >>>>>" + e);
            return null;
        }
    }

    static {
        try {
            System.loadLibrary("hwdeviceinfo");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load libarary hwdeviceinfo failed >>>>>" + e);
        }
    }
}
