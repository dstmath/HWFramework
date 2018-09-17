package com.android.server.am;

import android.os.RemoteException;
import android.util.Slog;
import com.huawei.android.app.IHwDAMonitorCallback;

class HwDAMonitorProxy {
    private static final String DEFAULT_EMPTY_RECENT_TASK = "";
    private static final int DEFAULT_GROUP_BG_VALUE = 1;
    private static final int DEFAULT_INVALID_INT_VALUE = -1;
    private static final int DEFAULT_MINI_COUNT = 0;
    private static final String TAG = "HwDAMonitorProxy";
    IHwDAMonitorCallback mDACallback = null;
    private boolean mIsSetCallback = false;

    HwDAMonitorProxy() {
    }

    public void registerDAMonitorCallback(IHwDAMonitorCallback callback) {
        this.mDACallback = callback;
        this.mIsSetCallback = true;
    }

    public int getActivityImportCount() {
        if (!this.mIsSetCallback) {
            return 0;
        }
        try {
            return this.mDACallback.getActivityImportCount();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return 0;
        }
    }

    public String getRecentTask() {
        if (!this.mIsSetCallback) {
            return DEFAULT_EMPTY_RECENT_TASK;
        }
        try {
            return this.mDACallback.getRecentTask();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return DEFAULT_EMPTY_RECENT_TASK;
        }
    }

    public int isCPUConfigWhiteList(String processName) {
        if (!this.mIsSetCallback) {
            return -1;
        }
        try {
            return this.mDACallback.isCPUConfigWhiteList(processName);
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return -1;
        }
    }

    public int getCPUConfigGroupBG() {
        if (!this.mIsSetCallback) {
            return 1;
        }
        try {
            return this.mDACallback.getCPUConfigGroupBG();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return 1;
        }
    }

    public boolean isCpusetEnable() {
        if (!this.mIsSetCallback) {
            return false;
        }
        try {
            return this.mDACallback.isCpusetEnable();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return false;
        }
    }

    public int getFirstDevSchedEventId() {
        if (!this.mIsSetCallback) {
            return -1;
        }
        try {
            return this.mDACallback.getFirstDevSchedEventId();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return -1;
        }
    }

    public int DAMonitorReport(int tag, String msg) {
        if (!this.mIsSetCallback) {
            return -1;
        }
        try {
            return this.mDACallback.DAMonitorReport(tag, msg);
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return -1;
        }
    }

    public void notifyActivityState(String activityInfo) {
        if (this.mIsSetCallback) {
            try {
                if (this.mDACallback != null) {
                    this.mDACallback.notifyActivityState(activityInfo);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "RemoteException thrown : ", e);
            }
        }
    }
}
