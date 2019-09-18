package com.android.server.power;

import android.os.RemoteException;
import android.util.Slog;
import com.huawei.android.os.IHwPowerDAMonitorCallback;

class HwPowerDAMonitorProxy {
    private static final String TAG = "HwPowerDAMonitorProxy";
    IHwPowerDAMonitorCallback mDACallback = null;
    private boolean mIsSetCallback = false;

    HwPowerDAMonitorProxy() {
    }

    public void registerPowerMonitorCallback(IHwPowerDAMonitorCallback callback) {
        if (callback != null && !this.mIsSetCallback) {
            this.mDACallback = callback;
            this.mIsSetCallback = true;
        }
    }

    public boolean isAwarePreventScreenOn(String pkgName, String tag) {
        if (!this.mIsSetCallback) {
            return false;
        }
        try {
            return this.mDACallback.isAwarePreventScreenOn(pkgName, tag);
        } catch (RemoteException e) {
            Slog.w(TAG, "reportData thrown RemoteException!");
            return false;
        }
    }
}
