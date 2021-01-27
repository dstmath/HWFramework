package com.huawei.android.feature.install.nonisolated;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.install.IFeatureInstall;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.signature.FeatureSignatureVerify;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.io.File;

public class NonIsolatedFeatureInstaller implements IFeatureInstall {
    private static final String TAG = NonIsolatedFeatureInstaller.class.getSimpleName();

    private boolean install(Context context, DynamicModuleInfo dynamicModuleInfo, boolean z) {
        if (dynamicModuleInfo == null || !z) {
            Log.d(TAG, "install: info == null or isAllowed");
            return false;
        }
        Log.d(TAG, "install " + dynamicModuleInfo.mModuleName + " installing");
        File nonIsolatedFeatureDexDir = InstallStorageManager.getNonIsolatedFeatureDexDir(context, dynamicModuleInfo.mVersionCode, dynamicModuleInfo.mModuleName);
        File nonIsolatedFeatureLibsDir = InstallStorageManager.getNonIsolatedFeatureLibsDir(context, dynamicModuleInfo.mVersionCode, dynamicModuleInfo.mModuleName);
        if (nonIsolatedFeatureDexDir == null || nonIsolatedFeatureLibsDir == null) {
            Log.d(TAG, "libs dir or dex dir is null");
            return false;
        }
        dynamicModuleInfo.mApkPath = new File(InstallStorageManager.getNonIsolatedVerifyDir(context, dynamicModuleInfo.mVersionCode), dynamicModuleInfo.mModuleName + dynamicModuleInfo.mSuffix).getAbsolutePath();
        dynamicModuleInfo.mDexDir = nonIsolatedFeatureDexDir.getAbsolutePath();
        dynamicModuleInfo.mNativeLibDir = nonIsolatedFeatureLibsDir.getAbsolutePath();
        DynamicModuleManager.getInstance().addInstalledModule(new NonIsolatedDynamicModule(context, dynamicModuleInfo));
        Log.d(TAG, "install: " + dynamicModuleInfo.mModuleName + " installed");
        return true;
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
        if (dynamicModuleInfo.mVersionCode == -1) {
            return -15;
        }
        File file = new File(InstallStorageManager.getNonIsolatedVerifyDir(context, dynamicModuleInfo.mVersionCode), dynamicModuleInfo.mModuleName + dynamicModuleInfo.mSuffix);
        File file2 = new File(dynamicModuleInfo.mTempPath);
        if (file.exists()) {
            InstallStorageManager.deleteFile(file2);
            return -16;
        } else if (!FeatureSignatureVerify.checkArchiveApkWithSelf(context, dynamicModuleInfo.mTempPath)) {
            return -12;
        } else {
            return !file2.renameTo(file) ? -17 : 0;
        }
    }
}
