package com.huawei.android.ota;

import android.os.SystemProperties;

public class HwOta {
    public static boolean hasOtaUpdated() {
        return SystemProperties.getBoolean("runtime.has_ota_update", false);
    }
}
