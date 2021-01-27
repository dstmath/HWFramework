package com.android.server.appwidget;

import android.appwidget.IHwAWSIDAMonitorCallback;
import android.os.RemoteException;
import android.util.Slog;

class HwAWSIDAMonitorProxy {
    private static final int DEFAULT_INVALID_INT_VALUE = -1;
    private static final String TAG = "HwAWSIDAMonitorProxy";
    IHwAWSIDAMonitorCallback mDACallback = null;
    private boolean mIsSetCallback = false;

    HwAWSIDAMonitorProxy() {
    }

    public void registerAWSIMonitorCallback(IHwAWSIDAMonitorCallback callback) {
        if (callback != null && !this.mIsSetCallback) {
            this.mDACallback = callback;
            this.mIsSetCallback = true;
        }
    }

    public void updateWidgetFlushReport(int userId, String packageName) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.updateWidgetFlushReport(userId, packageName);
            } catch (RemoteException e) {
                Slog.w(TAG, "updateWidgetFlushReport thrown RemoteException!");
            }
        }
    }
}
