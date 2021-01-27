package com.android.server.pm;

import android.content.pm.PackageParserEx;
import android.content.pm.PackageUserState;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.pm.permission.PermissionsStateEx;
import com.huawei.android.content.pm.PackageUserStateEx;
import java.io.File;

public class PackageSettingEx {
    private PackageSetting mPackageSetting;

    public File getCodePath() {
        PackageSetting packageSetting = this.mPackageSetting;
        if (packageSetting == null) {
            return null;
        }
        return packageSetting.codePath;
    }

    public PackageSetting getPackageSetting() {
        return this.mPackageSetting;
    }

    public void setPackageSetting(PackageSetting packageSetting) {
        this.mPackageSetting = packageSetting;
    }

    public int getAppUseNotchMode() {
        return this.mPackageSetting.getAppUseNotchMode();
    }

    public void setAppUseNotchMode(int mode) {
        this.mPackageSetting.setAppUseNotchMode(mode);
    }

    public int getAppUseSideMode() {
        return this.mPackageSetting.getAppUseSideMode();
    }

    public void setAppUseSideMode(int mode) {
        this.mPackageSetting.setAppUseSideMode(mode);
    }

    public PackageParserEx.PackageEx getPkg() {
        return new PackageParserEx.PackageEx(this.mPackageSetting.pkg);
    }

    public boolean disableComponentLPw(String componentClassName, int userId) {
        return this.mPackageSetting.disableComponentLPw(componentClassName, userId);
    }

    public boolean restoreComponentLPw(String componentClassName, int userId) {
        return this.mPackageSetting.restoreComponentLPw(componentClassName, userId);
    }

    public String getCodePathString() {
        PackageSetting packageSetting = this.mPackageSetting;
        if (packageSetting == null) {
            return null;
        }
        return packageSetting.codePathString;
    }

    public long getTimeStamp() {
        return this.mPackageSetting.timeStamp;
    }

    public PackageSignaturesEx getSignatures() {
        PackageSetting packageSetting = this.mPackageSetting;
        if (packageSetting == null) {
            return null;
        }
        PackageSignatures signatures = packageSetting.signatures;
        PackageSignaturesEx packageSignaturesEx = new PackageSignaturesEx();
        packageSignaturesEx.setPackageSignatures(signatures);
        return packageSignaturesEx;
    }

    public void setSignatures(PackageSignaturesEx signaturesEx) {
        this.mPackageSetting.signatures = signaturesEx.getPackageSignatures();
    }

    public long getVersionCode() {
        return this.mPackageSetting.versionCode;
    }

    public String[] getDisablePlugins() {
        return this.mPackageSetting.disablePlugins;
    }

    public void setDisablePlugins(String[] plugins) {
        this.mPackageSetting.disablePlugins = plugins;
    }

    public int[] queryInstalledUsers(int[] users, boolean installed) {
        return this.mPackageSetting.queryInstalledUsers(users, installed);
    }

    public float getAspectRatio(String aspectName) {
        return this.mPackageSetting.getAspectRatio(aspectName);
    }

    public void setAspectRatio(String aspectName, float ar) {
        this.mPackageSetting.setAspectRatio(aspectName, ar);
    }

    public boolean getInstalled(int userId) {
        return this.mPackageSetting.getInstalled(userId);
    }

    public boolean isAnyInstalled(int[] users) {
        return this.mPackageSetting.isAnyInstalled(users);
    }

    public void setForceDarkMode(int forceDark) {
        this.mPackageSetting.forceDarkMode = forceDark;
    }

    public int getForceDarkMode() {
        return this.mPackageSetting.forceDarkMode;
    }

    public PermissionsStateEx getPermissionsState() {
        PermissionsState state = this.mPackageSetting.getPermissionsState();
        PermissionsStateEx stateEx = new PermissionsStateEx();
        stateEx.setPermissionState(state);
        return stateEx;
    }

    public PackageUserStateEx readUserState(int userId) {
        PackageUserState userState = this.mPackageSetting.readUserState(userId);
        PackageUserStateEx stateEx = new PackageUserStateEx();
        stateEx.setPackageUserState(userState);
        return stateEx;
    }

    public long getCeDataInode(int userId) {
        return this.mPackageSetting.getCeDataInode(userId);
    }

    public boolean isObjNull() {
        return this.mPackageSetting == null;
    }
}
