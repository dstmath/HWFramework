package com.huawei.android.iaware;

import android.os.IBinder;
import android.rms.iaware.IAwareSdk;

public class IAwareSdkEx {
    public static final int THERMAL_SDK_CALLBACK_EVENT_ID = 3034;

    public static void reportData(int resId, String message) {
        IAwareSdk.asyncReportData(resId, message, 0);
    }

    public static void registerCallback(int resId, String description, IBinder callback) {
        IAwareSdk.asyncReportDataWithCallback(resId, description, callback, 0);
    }
}
