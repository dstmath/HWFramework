package com.android.server.pm;

import android.content.Context;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;

public class ProtectedPackages {
    private final Context mContext;
    @GuardedBy("this")
    private String mDeviceOwnerPackage;
    @GuardedBy("this")
    private int mDeviceOwnerUserId;
    @GuardedBy("this")
    private final String mDeviceProvisioningPackage = this.mContext.getResources().getString(17039775);
    @GuardedBy("this")
    private SparseArray<String> mProfileOwnerPackages;

    public ProtectedPackages(Context context) {
        this.mContext = context;
    }

    public synchronized void setDeviceAndProfileOwnerPackages(int deviceOwnerUserId, String deviceOwnerPackage, SparseArray<String> profileOwnerPackages) {
        SparseArray sparseArray = null;
        synchronized (this) {
            this.mDeviceOwnerUserId = deviceOwnerUserId;
            if (deviceOwnerUserId == -10000) {
                deviceOwnerPackage = null;
            }
            this.mDeviceOwnerPackage = deviceOwnerPackage;
            if (profileOwnerPackages != null) {
                sparseArray = profileOwnerPackages.clone();
            }
            this.mProfileOwnerPackages = sparseArray;
        }
    }

    /* JADX WARNING: Missing block: B:22:0x002c, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean hasDeviceOwnerOrProfileOwner(int userId, String packageName) {
        if (packageName == null) {
            return false;
        }
        if (this.mDeviceOwnerPackage != null && this.mDeviceOwnerUserId == userId && packageName.equals(this.mDeviceOwnerPackage)) {
            return true;
        }
        if (this.mProfileOwnerPackages != null && packageName.equals(this.mProfileOwnerPackages.get(userId))) {
            return true;
        }
    }

    private synchronized boolean isProtectedPackage(String packageName) {
        return packageName != null ? packageName.equals(this.mDeviceProvisioningPackage) : false;
    }

    public boolean isPackageStateProtected(int userId, String packageName) {
        if (hasDeviceOwnerOrProfileOwner(userId, packageName)) {
            return true;
        }
        return isProtectedPackage(packageName);
    }

    public boolean isPackageDataProtected(int userId, String packageName) {
        if (hasDeviceOwnerOrProfileOwner(userId, packageName)) {
            return true;
        }
        return isProtectedPackage(packageName);
    }
}
