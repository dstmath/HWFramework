package com.android.server.devicepolicy;

import android.app.admin.DevicePolicyCache;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;

public class DevicePolicyCacheImpl extends DevicePolicyCache {
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private final SparseBooleanArray mScreenCaptureDisabled = new SparseBooleanArray();

    public void onUserRemoved(int userHandle) {
        synchronized (this.mLock) {
            this.mScreenCaptureDisabled.delete(userHandle);
        }
    }

    public boolean getScreenCaptureDisabled(int userHandle) {
        boolean z;
        synchronized (this.mLock) {
            z = this.mScreenCaptureDisabled.get(userHandle);
        }
        return z;
    }

    public void setScreenCaptureDisabled(int userHandle, boolean disabled) {
        synchronized (this.mLock) {
            this.mScreenCaptureDisabled.put(userHandle, disabled);
        }
    }
}
