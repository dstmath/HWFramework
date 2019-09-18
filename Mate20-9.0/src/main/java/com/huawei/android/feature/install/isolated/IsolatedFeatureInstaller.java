package com.huawei.android.feature.install.isolated;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.install.IFeatureInstall;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleManager;
import com.huawei.android.feature.utils.HiPkgSignManager;
import java.io.File;

public class IsolatedFeatureInstaller implements IFeatureInstall {
    private static final String TAG = IsolatedFeatureInstaller.class.getSimpleName();

    private boolean install(Context context, DynamicModuleInfo dynamicModuleInfo, boolean z) {
        if (dynamicModuleInfo == null || !z) {
            Log.d(TAG, "install not installed");
            return false;
        }
        Log.d(TAG, "install " + dynamicModuleInfo.mModuleName + " installing");
        File isolatedModuleDir = InstallStorageManager.getIsolatedModuleDir(context, dynamicModuleInfo.mModuleName);
        File isolatedModuleDexDir = InstallStorageManager.getIsolatedModuleDexDir(context, dynamicModuleInfo.mModuleName);
        File isolatedModuleNativeDir = InstallStorageManager.getIsolatedModuleNativeDir(context, dynamicModuleInfo.mModuleName);
        if (isolatedModuleDir == null || isolatedModuleDexDir == null || isolatedModuleNativeDir == null) {
            return false;
        }
        dynamicModuleInfo.mApkPath = new File(isolatedModuleDir, dynamicModuleInfo.mModuleName + dynamicModuleInfo.mSuffix).getAbsolutePath();
        dynamicModuleInfo.mDexDir = isolatedModuleDexDir.getAbsolutePath();
        dynamicModuleInfo.mNativeLibDir = isolatedModuleNativeDir.getAbsolutePath();
        DynamicModuleManager.getInstance().addInstalledModule(new IsolatedDynamicModule(context, dynamicModuleInfo));
        Log.d(TAG, "install: " + dynamicModuleInfo.mModuleName + " installed");
        return true;
    }

    public int installFeatureFromUnverifyIfNeed(Context context, DynamicModuleInfo dynamicModuleInfo) {
        int moveToVerifyIfNeed = moveToVerifyIfNeed(context, dynamicModuleInfo);
        install(context, dynamicModuleInfo, moveToVerifyIfNeed == 0);
        return moveToVerifyIfNeed;
    }

    public boolean installFeatureFromVerify(Context context, DynamicModuleInfo dynamicModuleInfo) {
        return install(context, dynamicModuleInfo, true);
    }

    public int moveToVerifyIfNeed(Context context, DynamicModuleInfo dynamicModuleInfo) {
        File file = new File(dynamicModuleInfo.mTempPath);
        File isolatedModuleDir = InstallStorageManager.getIsolatedModuleDir(context, dynamicModuleInfo.mModuleName);
        File file2 = new File(isolatedModuleDir, dynamicModuleInfo.mModuleName + dynamicModuleInfo.mSuffix);
        if (isolatedModuleDir == null) {
            return -11;
        }
        if (dynamicModuleInfo.mSignatureVerifyType == 1) {
            if (dynamicModuleInfo.mExpectSignInfo == null) {
                return -12;
            }
            if (!HiPkgSignManager.doCheckArchiveApk(context, dynamicModuleInfo.mExpectSignInfo, dynamicModuleInfo.mTempPath, dynamicModuleInfo.mModuleName)) {
                return -12;
            }
        }
        if (!file2.exists()) {
            return file.renameTo(file2) ? 0 : -17;
        }
        DynamicModuleInfo parseApk = DynamicModuleManager.parseApk(context, file2.getAbsolutePath());
        if (parseApk == null) {
            return -11;
        }
        Log.d(TAG, "origin versionCode " + parseApk.mVersionCode + " -- > install VersionCode " + dynamicModuleInfo.mVersionCode);
        if (dynamicModuleInfo.mVersionCode <= parseApk.mVersionCode) {
            InstallStorageManager.deleteFile(file);
            return -13;
        } else if (DynamicModuleManager.getInstance().isMoudleInstalled(dynamicModuleInfo.mModuleName)) {
            return file.renameTo(new File(InstallStorageManager.getIsolateUpdateDir(context, dynamicModuleInfo.mModuleName), new StringBuilder().append(dynamicModuleInfo.mModuleName).append(dynamicModuleInfo.mSuffix).toString())) ? -14 : -17;
        } else {
            File[] listFiles = isolatedModuleDir.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File deleteFile : listFiles) {
                    InstallStorageManager.deleteFile(deleteFile);
                }
            }
            return file.renameTo(file2) ? 0 : -17;
        }
    }
}
