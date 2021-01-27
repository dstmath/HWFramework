package com.huawei.android.content.pm;

import android.content.pm.VersionedPackage;

public class VersionedPackageEx {
    private VersionedPackage mVersionedPackage;

    public VersionedPackageEx(String packageName, int versionCode) {
        this.mVersionedPackage = new VersionedPackage(packageName, versionCode);
    }

    public VersionedPackage getVersionedPackage() {
        return this.mVersionedPackage;
    }

    public void setVersionedPackage(VersionedPackage versionedPackage) {
        this.mVersionedPackage = versionedPackage;
    }
}
