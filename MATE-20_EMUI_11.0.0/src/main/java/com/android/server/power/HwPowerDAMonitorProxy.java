package com.android.server.power;

import android.os.RemoteException;
import android.util.Slog;
import com.android.server.power.PowerManagerService;
import com.huawei.android.os.IHwPowerDAMonitorCallback;

public class HwPowerDAMonitorProxy {
    private static final String TAG = "HwPowerDAMonitorProxy";
    IHwPowerDAMonitorCallback mDACallback = null;
    private boolean mIsSetCallback = false;

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

    public void notifyWakeLockReleaseToIAware(PowerManagerService.WakeLock wakeLock) {
        if (this.mIsSetCallback) {
            if ((wakeLock.mFlags & 65535) == 26 || (wakeLock.mFlags & 65535) == 10) {
                try {
                    this.mDACallback.notifyWakeLockReleaseToIAware(wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mPackageName, wakeLock.mTag);
                } catch (RemoteException e) {
                    Slog.w(TAG, "reportData thrown RemoteException!");
                }
            }
        }
    }

    public void notifyWakeLockToIAware(PowerManagerService.WakeLock wakeLock) {
        if (this.mIsSetCallback) {
            if ((wakeLock.mFlags & 65535) == 26 || (wakeLock.mFlags & 65535) == 10) {
                try {
                    this.mDACallback.notifyWakeLockToIAware(wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mPackageName, wakeLock.mTag);
                } catch (RemoteException e) {
                    Slog.w(TAG, "reportData thrown RemoteException!");
                }
            }
        }
    }
}
