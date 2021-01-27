package com.huawei.android.feature.install.isolated;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.fingerprint.SignVerifyStrategy;
import com.huawei.android.feature.fingerprint.SignVerifyStrategyFactory;
import com.huawei.android.feature.install.IFeatureInstall;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleManager;
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

    private int moveToVerify(Context context, DynamicModuleInfo dynamicModuleInfo, File file, File file2) {
        File file3 = new File(dynamicModuleInfo.mTempPath);
        DynamicModuleInfo parseApk = DynamicModuleManager.parseApk(context, file2.getAbsolutePath());
        if (parseApk == null) {
            return -11;
        }
        Log.d(TAG, "origin versionCode " + parseApk.mVersionCode + " -- > install VersionCode " + dynamicModuleInfo.mVersionCode);
        if (dynamicModuleInfo.mVersionCode <= parseApk.mVersionCode) {
            InstallStorageManager.deleteFile(file3);
            return -13;
        } else if (DynamicModuleManager.getInstance().isMoudleInstalled(dynamicModuleInfo.mModuleName)) {
            return file3.renameTo(new File(InstallStorageManager.getIsolateUpdateDir(context, dynamicModuleInfo.mModuleName), new StringBuilder().append(dynamicModuleInfo.mModuleName).append(dynamicModuleInfo.mSuffix).toString())) ? -14 : -17;
        } else {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file4 : listFiles) {
                    InstallStorageManager.deleteFile(file4);
                }
            }
            return !file3.renameTo(file2) ? -17 : 0;
        }
    }

    @Override // com.huawei.android.feature.install.IFeatureInstall
    public int installFeatureFromUnverifyIfNeed(Context context, DynamicModuleInfo dynamicModuleInfo) {
        int moveToVerifyIfNeed = moveToVerifyIfNeed(context, dynamicModuleInfo);
        install(context, dynamicModuleInfo, moveToVerifyIfNeed == 0);
        return moveToVerifyIfNeed;
    }

    @Override // com.huawei.android.feature.install.IFeatureInstall
    public boolean installFeatureFromVerify(Context context, DynamicModuleInfo dynamicModuleInfo) {
        return install(context, dynamicModuleInfo, true);
    }

    @Override // com.huawei.android.feature.install.IFeatureInstall
    public int moveToVerifyIfNeed(Context context, DynamicModuleInfo dynamicModuleInfo) {
        File file = new File(dynamicModuleInfo.mTempPath);
        File isolatedModuleDir = InstallStorageManager.getIsolatedModuleDir(context, dynamicModuleInfo.mModuleName);
        File file2 = new File(isolatedModuleDir, dynamicModuleInfo.mModuleName + dynamicModuleInfo.mSuffix);
        if (isolatedModuleDir == null) {
            return -11;
        }
        SignVerifyStrategy signVerifyStrategy = SignVerifyStrategyFactory.getSignVerifyStrategy(dynamicModuleInfo.mSignatureVerifyType);
        if (signVerifyStrategy == null) {
            Log.e(TAG, "invalid SignatureVerifyType, there must be a verify type.");
            return -12;
        } else if (signVerifyStrategy.verifyFingerPrint(context, dynamicModuleInfo)) {
            return file2.exists() ? moveToVerify(context, dynamicModuleInfo, isolatedModuleDir, file2) : file.renameTo(file2) ? 0 : -17;
        } else {
            Log.w(TAG, "verifySignAndFingerPrint failed.");
            return -12;
        }
    }
}
