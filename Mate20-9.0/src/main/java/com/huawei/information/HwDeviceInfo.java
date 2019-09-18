package com.huawei.information;

import android.util.Log;

public class HwDeviceInfo {
    private static final String TAG = "HwDeviceInfo";
    private static final Object lock = new Object();

    private static native String get_emmc_id();

    static {
        try {
            System.loadLibrary("hwdeviceinfo");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load libarary hwdeviceinfo failed >>>>>" + e);
        }
    }

    public static String getEMMCID() {
        String str;
        synchronized (lock) {
            try {
                Log.d(TAG, "HwDeviceInfo 64bits so, getEMMCID");
                str = get_emmc_id();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "libarary hwdeviceinfo get_emmc_id failed >>>>>" + e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return str;
    }
}
