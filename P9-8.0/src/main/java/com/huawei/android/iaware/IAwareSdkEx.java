package com.huawei.android.iaware;

import android.rms.iaware.IAwareSdk;

public class IAwareSdkEx {
    public static void reportData(int resId, String message) {
        IAwareSdk.asyncReportData(resId, message, 0);
    }
}
