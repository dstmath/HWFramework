package com.android.server.rms.iaware;

import com.android.server.rms.iaware.feature.DevSchedFeatureRt;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.huawei.android.os.IHwPowerDAMonitorCallback;

class AwarePowerMonitorCallback extends IHwPowerDAMonitorCallback.Stub {
    private static final String TAG = "AwarePowerDAMonitorCallback";

    AwarePowerMonitorCallback() {
    }

    public boolean isAwarePreventScreenOn(String pkgName, String tag) {
        return DevSchedFeatureRt.isAwarePreventWakelockScreenOn(pkgName, tag);
    }

    public void notifyWakeLockToIAware(int uid, int pid, String packageName, String tag) {
        SysLoadManager.getInstance().notifyWakeLock(uid, pid, packageName, tag);
    }

    public void notifyWakeLockReleaseToIAware(int uid, int pid, String packageName, String tag) {
        SysLoadManager.getInstance().notifyWakeLockRelease(uid, pid, packageName, tag);
    }
}
