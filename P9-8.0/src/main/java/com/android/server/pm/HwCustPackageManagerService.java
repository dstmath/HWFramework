package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageParser.Package;
import android.content.pm.ResolveInfo;
import android.os.storage.VolumeInfo;
import android.util.Log;
import java.io.File;
import java.util.List;

public class HwCustPackageManagerService {
    static final String TAG = "HwCustPackageManagerService";

    public HwCustPackageManagerService() {
        Log.d(TAG, TAG);
    }

    public void scanCustPrivDir(int scanMode, AbsPackageManagerService service) {
    }

    public boolean isPrivAppInCust(File file) {
        return false;
    }

    public void handleCustInitailizations(Object settings) {
    }

    public File customizeUninstallApk(File file) {
        return file;
    }

    public boolean isMccMncMatch() {
        return false;
    }

    public String getCustomizeAPKListFile(String apkListFile, String mAPKInstallList_FILE, String mDelAPKInstallList_FILE, String mAPKInstallList_DIR) {
        return apkListFile;
    }

    public String getCustomizeAPKInstallFile(String APKInstallFile, String DelAPKInstallFile) {
        return APKInstallFile;
    }

    public String getCustomizeDelAPKInstallFile(String APKInstallFile, String DelAPKInstallFile) {
        return DelAPKInstallFile;
    }

    public boolean isHwCustHiddenInfoPackage(Package pkgInfo) {
        return false;
    }

    public boolean needDerivePkgAbi(Package pkg) {
        return false;
    }

    public boolean canAppMoveToPublicSd(VolumeInfo volume) {
        return false;
    }

    public boolean isSdInstallEnabled() {
        return false;
    }

    public boolean isSdVol(VolumeInfo vol) {
        return false;
    }

    public int isListedApp(String packageName) {
        return -1;
    }

    public boolean isHwFiltReqInstallPerm(String pkgName, String permission) {
        return false;
    }

    public boolean isUnAppInstallAllowed(String originPath, Context context) {
        return false;
    }

    public boolean isSkipMmsSendImageAction() {
        return false;
    }

    public List<ResolveInfo> filterResolveInfos(List<ResolveInfo> rInfos, Intent intent, String resolvedType) {
        return rInfos;
    }

    public String getCustDefaultLauncher(Context context) {
        return null;
    }
}
