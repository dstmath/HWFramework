package com.android.server.wm;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Slog;
import com.huawei.android.app.IHwAtmDAMonitorCallback;

/* access modifiers changed from: package-private */
public class HwAtmDAMonitorProxy {
    private static final String TAG = "HwDAMonitorProxy";
    IHwAtmDAMonitorCallback mDACallback = null;
    private boolean mIsSetCallback = false;

    HwAtmDAMonitorProxy() {
    }

    public void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) {
        if (callback != null && !this.mIsSetCallback) {
            this.mDACallback = callback;
            this.mIsSetCallback = true;
        }
    }

    public void noteActivityStart(String packageName, String processName, String activityName, int pid, int uid, boolean started) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.noteActivityStart(new String[]{packageName, processName, activityName}, pid, uid, started);
            } catch (RemoteException e) {
                Slog.w(TAG, "noteActivityStart thrown RemoteException!");
            }
        }
    }

    public void noteActivityDisplayed(String componentName, int uid, int pid, boolean isStart) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.noteActivityDisplayed(componentName, uid, pid, isStart);
            } catch (RemoteException e) {
                Slog.w(TAG, "noteActivityDisplayed thrown RemoteException!");
            }
        }
    }

    public void notifyAppEventToIaware(int type, String packageName) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.notifyAppEventToIaware(type, packageName);
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyAppEventToIaware thrown RemoteException!");
            }
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

    public void recognizeFakeActivity(String compName, int pid, int uid) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.recognizeFakeActivity(compName, pid, uid);
            } catch (RemoteException e) {
                Slog.w(TAG, "recognizeFakeActivity thrown RemoteException!");
            }
        }
    }

    public void notifyActivityState(String activityInfo) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.notifyActivityState(activityInfo);
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyActivityState thrown RemoteException!");
            }
        }
    }
}
