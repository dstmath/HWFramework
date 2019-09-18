package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.UserInfo;
import android.util.proto.ProtoOutputStream;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.slice.SliceClientPermissions;
import java.io.File;
import java.util.List;

public final class PackageSetting extends PackageSettingBase {
    int appId;
    PackageParser.Package pkg;
    SharedUserSetting sharedUser;
    private int sharedUserId;

    PackageSetting(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, long pVersionCode, int pkgFlags, int privateFlags, String parentPackageName, List<String> childPackageNames, int sharedUserId2, String[] usesStaticLibraries, long[] usesStaticLibrariesVersions) {
        super(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, pVersionCode, pkgFlags, privateFlags, parentPackageName, childPackageNames, usesStaticLibraries, usesStaticLibrariesVersions);
        this.sharedUserId = sharedUserId2;
    }

    PackageSetting(PackageSetting orig) {
        super(orig, orig.realName);
        doCopy(orig);
    }

    PackageSetting(PackageSetting orig, String realPkgName) {
        super(orig, realPkgName);
        doCopy(orig);
    }

    public int getSharedUserId() {
        if (this.sharedUser != null) {
            return this.sharedUser.userId;
        }
        return this.sharedUserId;
    }

    public SharedUserSetting getSharedUser() {
        return this.sharedUser;
    }

    public String toString() {
        return "PackageSetting{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.name + SliceClientPermissions.SliceAuthority.DELIMITER + this.appId + "}";
    }

    public void copyFrom(PackageSetting orig) {
        super.copyFrom((PackageSettingBase) orig);
        doCopy(orig);
    }

    private void doCopy(PackageSetting orig) {
        this.appId = orig.appId;
        this.pkg = orig.pkg;
        this.sharedUser = orig.sharedUser;
        this.sharedUserId = orig.sharedUserId;
    }

    public PermissionsState getPermissionsState() {
        if (this.sharedUser != null) {
            return this.sharedUser.getPermissionsState();
        }
        return super.getPermissionsState();
    }

    public PackageParser.Package getPackage() {
        return this.pkg;
    }

    public int getAppId() {
        return this.appId;
    }

    public void setInstallPermissionsFixed(boolean fixed) {
        this.installPermissionsFixed = fixed;
    }

    public boolean areInstallPermissionsFixed() {
        return this.installPermissionsFixed;
    }

    public boolean isPrivileged() {
        return (this.pkgPrivateFlags & 8) != 0;
    }

    public boolean isOem() {
        return (this.pkgPrivateFlags & 131072) != 0;
    }

    public boolean isVendor() {
        return (this.pkgPrivateFlags & 262144) != 0;
    }

    public boolean isProduct() {
        return (this.pkgPrivateFlags & DumpState.DUMP_FROZEN) != 0;
    }

    public boolean isForwardLocked() {
        return (this.pkgPrivateFlags & 4) != 0;
    }

    public boolean isSystem() {
        return (this.pkgFlags & 1) != 0;
    }

    public boolean isUpdatedSystem() {
        return (this.pkgFlags & 128) != 0;
    }

    public boolean isSharedUser() {
        return this.sharedUser != null;
    }

    public boolean isMatch(int flags) {
        if ((1048576 & flags) != 0) {
            return isSystem();
        }
        return true;
    }

    public boolean hasChildPackages() {
        return this.childPackageNames != null && !this.childPackageNames.isEmpty();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, List<UserInfo> list) {
        ProtoOutputStream protoOutputStream = proto;
        long packageToken = proto.start(fieldId);
        protoOutputStream.write(1138166333441L, this.realName != null ? this.realName : this.name);
        protoOutputStream.write(1120986464258L, this.appId);
        protoOutputStream.write(1120986464259L, this.versionCode);
        protoOutputStream.write(1138166333444L, this.pkg.mVersionName);
        protoOutputStream.write(1112396529669L, this.firstInstallTime);
        protoOutputStream.write(1112396529670L, this.lastUpdateTime);
        protoOutputStream.write(1138166333447L, this.installerPackageName);
        if (this.pkg != null) {
            long splitToken = protoOutputStream.start(2246267895816L);
            protoOutputStream.write(1138166333441L, "base");
            protoOutputStream.write(1120986464258L, this.pkg.baseRevisionCode);
            protoOutputStream.end(splitToken);
            if (this.pkg.splitNames != null) {
                for (int i = 0; i < this.pkg.splitNames.length; i++) {
                    long splitToken2 = protoOutputStream.start(2246267895816L);
                    protoOutputStream.write(1138166333441L, this.pkg.splitNames[i]);
                    protoOutputStream.write(1120986464258L, this.pkg.splitRevisionCodes[i]);
                    protoOutputStream.end(splitToken2);
                }
            }
        }
        writeUsersInfoToProto(protoOutputStream, 2246267895817L);
        protoOutputStream.end(packageToken);
    }
}
