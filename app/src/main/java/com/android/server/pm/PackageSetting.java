package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import java.io.File;
import java.util.List;

final class PackageSetting extends PackageSettingBase {
    int appId;
    Package pkg;
    SharedUserSetting sharedUser;

    PackageSetting(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int pVersionCode, int pkgFlags, int privateFlags, String parentPackageName, List<String> childPackageNames) {
        super(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, pVersionCode, pkgFlags, privateFlags, parentPackageName, childPackageNames);
    }

    PackageSetting(PackageSetting orig) {
        super(orig);
        this.appId = orig.appId;
        this.pkg = orig.pkg;
        this.sharedUser = orig.sharedUser;
    }

    public String toString() {
        return "PackageSetting{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.name + "/" + this.appId + "}";
    }

    public PermissionsState getPermissionsState() {
        if (this.sharedUser != null) {
            return this.sharedUser.getPermissionsState();
        }
        return super.getPermissionsState();
    }

    public boolean isPrivileged() {
        return (this.pkgPrivateFlags & 8) != 0;
    }

    public boolean isForwardLocked() {
        return (this.pkgPrivateFlags & 4) != 0;
    }

    public boolean isSystem() {
        return (this.pkgFlags & 1) != 0;
    }

    public boolean isSharedUser() {
        return this.sharedUser != null;
    }

    public boolean isMatch(int flags) {
        if ((DumpState.DUMP_DEXOPT & flags) != 0) {
            return isSystem();
        }
        return true;
    }
}
