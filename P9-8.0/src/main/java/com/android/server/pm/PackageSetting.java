package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.content.pm.UserInfo;
import android.util.proto.ProtoOutputStream;
import java.io.File;
import java.util.List;

final class PackageSetting extends PackageSettingBase {
    int appId;
    Package pkg;
    SharedUserSetting sharedUser;
    private int sharedUserId;

    PackageSetting(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int pVersionCode, int pkgFlags, int privateFlags, String parentPackageName, List<String> childPackageNames, int sharedUserId, String[] usesStaticLibraries, int[] usesStaticLibrariesVersions) {
        super(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, pVersionCode, pkgFlags, privateFlags, parentPackageName, childPackageNames, usesStaticLibraries, usesStaticLibrariesVersions);
        this.sharedUserId = sharedUserId;
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

    public String toString() {
        return "PackageSetting{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.name + "/" + this.appId + "}";
    }

    public void copyFrom(PackageSetting orig) {
        super.copyFrom(orig);
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

    public void writeToProto(ProtoOutputStream proto, long fieldId, List<UserInfo> list) {
        long packageToken = proto.start(fieldId);
        proto.write(1159641169921L, this.realName != null ? this.realName : this.name);
        proto.write(1112396529666L, this.appId);
        proto.write(1112396529667L, this.versionCode);
        proto.write(1159641169924L, this.pkg.mVersionName);
        proto.write(1116691496965L, this.firstInstallTime);
        proto.write(1116691496966L, this.lastUpdateTime);
        proto.write(1159641169927L, this.installerPackageName);
        if (this.pkg != null) {
            long splitToken = proto.start(2272037699592L);
            proto.write(1159641169921L, "base");
            proto.write(1112396529666L, this.pkg.baseRevisionCode);
            proto.end(splitToken);
            if (this.pkg.splitNames != null) {
                for (int i = 0; i < this.pkg.splitNames.length; i++) {
                    splitToken = proto.start(2272037699592L);
                    proto.write(1159641169921L, this.pkg.splitNames[i]);
                    proto.write(1112396529666L, this.pkg.splitRevisionCodes[i]);
                    proto.end(splitToken);
                }
            }
        }
        writeUsersInfoToProto(proto, 2272037699593L);
        proto.end(packageToken);
    }
}
