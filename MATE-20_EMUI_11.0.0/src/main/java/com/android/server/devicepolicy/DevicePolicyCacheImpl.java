package com.android.server.devicepolicy;

import android.app.admin.DevicePolicyCache;
import android.content.Intent;
import android.hdm.HwDeviceManager;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintWriter;

public class DevicePolicyCacheImpl extends DevicePolicyCache {
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private final SparseIntArray mPasswordQuality = new SparseIntArray();
    @GuardedBy({"mLock"})
    private final SparseBooleanArray mScreenCaptureDisabled = new SparseBooleanArray();

    public void onUserRemoved(int userHandle) {
        synchronized (this.mLock) {
            this.mScreenCaptureDisabled.delete(userHandle);
            this.mPasswordQuality.delete(userHandle);
        }
    }

    public boolean getScreenCaptureDisabled(int userHandle) {
        boolean z;
        synchronized (this.mLock) {
            if (!this.mScreenCaptureDisabled.get(userHandle)) {
                if (!HwDeviceManager.mdmDisallowOp(20, (Intent) null)) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public void setScreenCaptureDisabled(int userHandle, boolean disabled) {
        synchronized (this.mLock) {
            this.mScreenCaptureDisabled.put(userHandle, disabled);
        }
    }

    public int getPasswordQuality(int userHandle) {
        int i;
        synchronized (this.mLock) {
            i = this.mPasswordQuality.get(userHandle, 0);
        }
        return i;
    }

    public void setPasswordQuality(int userHandle, int quality) {
        synchronized (this.mLock) {
            this.mPasswordQuality.put(userHandle, quality);
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println("Device policy cache");
        pw.println(prefix + "Screen capture disabled: " + this.mScreenCaptureDisabled.toString());
        pw.println(prefix + "Password quality: " + this.mPasswordQuality.toString());
    }
}
