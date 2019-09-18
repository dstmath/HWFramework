package com.android.server.rms.iaware;

import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.huawei.android.os.IHwPowerDAMonitorCallback;

class AwarePowerMonitorCallback extends IHwPowerDAMonitorCallback.Stub {
    private static final String TAG = "AwarePowerDAMonitorCallback";

    AwarePowerMonitorCallback() {
    }

    public boolean isAwarePreventScreenOn(String pkgName, String tag) {
        return DevSchedFeatureRT.isAwarePreventWakelockScreenOn(pkgName, tag);
    }
}
