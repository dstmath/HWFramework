package com.android.server.wm;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Slog;
import com.huawei.android.view.IHwWMDAMonitorCallback;

class HwWMDAMonitorProxy {
    private static final String TAG = "HwWMDAMonitorProxy";
    IHwWMDAMonitorCallback mDACallback = null;
    private boolean mIsSetCallback = false;

    HwWMDAMonitorProxy() {
    }

    public void registerWMMonitorCallback(IHwWMDAMonitorCallback callback) {
        if (callback != null && !this.mIsSetCallback) {
            this.mDACallback = callback;
            this.mIsSetCallback = true;
        }
    }

    public boolean isResourceNeeded(String resourceid) {
        if (!this.mIsSetCallback) {
            return false;
        }
        try {
            return this.mDACallback.isResourceNeeded(resourceid);
        } catch (RemoteException e) {
            Slog.w(TAG, "isResourceNeeded thrown RemoteException!");
            return false;
        }
    }

    public void reportData(String resourceid, long timestamp, Bundle args) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.reportData(resourceid, timestamp, args);
            } catch (RemoteException e) {
                Slog.w(TAG, "reportData thrown RemoteException!");
            }
        }
    }
}
