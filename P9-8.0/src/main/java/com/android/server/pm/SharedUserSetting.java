package com.android.server.pm;

import android.util.ArraySet;

final class SharedUserSetting extends SettingBase {
    final String name;
    final ArraySet<PackageSetting> packages = new ArraySet();
    final PackageSignatures signatures = new PackageSignatures();
    int uidFlags;
    int uidPrivateFlags;
    int userId;

    SharedUserSetting(String _name, int _pkgFlags, int _pkgPrivateFlags) {
        super(_pkgFlags, _pkgPrivateFlags);
        this.uidFlags = _pkgFlags;
        this.uidPrivateFlags = _pkgPrivateFlags;
        this.name = _name;
    }

    public String toString() {
        return "SharedUserSetting{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.name + "/" + this.userId + "}";
    }

    void removePackage(PackageSetting packageSetting) {
        if (this.packages.remove(packageSetting)) {
            if ((this.pkgFlags & packageSetting.pkgFlags) != 0) {
                int aggregatedFlags = this.uidFlags;
                for (PackageSetting ps : this.packages) {
                    aggregatedFlags |= ps.pkgFlags;
                }
                setFlags(aggregatedFlags);
            }
            if ((this.pkgPrivateFlags & packageSetting.pkgPrivateFlags) != 0) {
                int aggregatedPrivateFlags = this.uidPrivateFlags;
                for (PackageSetting ps2 : this.packages) {
                    aggregatedPrivateFlags |= ps2.pkgPrivateFlags;
                }
                setPrivateFlags(aggregatedPrivateFlags);
            }
        }
    }

    void addPackage(PackageSetting packageSetting) {
        if (this.packages.add(packageSetting)) {
            setFlags(this.pkgFlags | packageSetting.pkgFlags);
            setPrivateFlags(this.pkgPrivateFlags | packageSetting.pkgPrivateFlags);
        }
    }
}
