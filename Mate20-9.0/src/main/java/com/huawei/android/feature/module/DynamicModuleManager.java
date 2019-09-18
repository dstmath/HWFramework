package com.huawei.android.feature.module;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;
import com.huawei.android.feature.install.BasePackageInfoManager;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.config.FeatureInstallConfig;
import com.huawei.android.feature.install.isolated.IsolatedFeatureInstaller;
import com.huawei.android.feature.install.nonisolated.NonIsolatedFeatureInstaller;
import java.io.File;
import java.util.HashMap;

public class DynamicModuleManager {
    private static final String TAG = DynamicModuleManager.class.getSimpleName();
    private static volatile DynamicModuleManager sInstance;
    private HashMap<String, DynamicFeatureState> mDynamicFeatureStates = new HashMap<>();
    private HashMap<String, DynamicModuleInternal> mDynamicModules = new HashMap<>();

    public static DynamicModuleManager getInstance() {
        if (sInstance == null) {
            synchronized (DynamicModuleManager.class) {
                if (sInstance == null) {
                    sInstance = new DynamicModuleManager();
                }
            }
        }
        return sInstance;
    }

    public static int installUnverifyFeatures(Context context, String str, String str2) {
        DynamicModuleInfo parseUnverifyApk = parseUnverifyApk(context, str, str2);
        if (parseUnverifyApk == null) {
            return -11;
        }
        int installFeatureFromUnverifyIfNeed = (parseUnverifyApk.mIsIsolated == 1 ? new IsolatedFeatureInstaller() : new NonIsolatedFeatureInstaller()).installFeatureFromUnverifyIfNeed(context, parseUnverifyApk);
        if (installFeatureFromUnverifyIfNeed != 0) {
            return installFeatureFromUnverifyIfNeed;
        }
        getInstance().getDynamicModule(parseUnverifyApk.mModuleName).install();
        return installFeatureFromUnverifyIfNeed;
    }

    public static DynamicModuleInfo parseApk(Context context, String str) {
        Log.d(TAG, "parseApk");
        PackageInfo packageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(new File(str).getAbsolutePath(), 128);
        if (packageArchiveInfo == null) {
            return null;
        }
        int i = packageArchiveInfo.applicationInfo.metaData.getInt("feature_isolated_type", 0);
        int i2 = packageArchiveInfo.applicationInfo.metaData.getInt("feature_auto_load", 0);
        String string = packageArchiveInfo.applicationInfo.metaData.getString("feature_name");
        long versionCode = i == 0 ? BasePackageInfoManager.getInstance(context).getVersionCode() : (long) packageArchiveInfo.versionCode;
        Log.d(TAG, "isolatedType : " + i + " autoLoadType : " + i2 + " versionCode : " + versionCode);
        DynamicModuleInfo dynamicModuleInfo = new DynamicModuleInfo();
        dynamicModuleInfo.mIsIsolated = i;
        dynamicModuleInfo.mSignatureVerifyType = 1;
        dynamicModuleInfo.mModuleName = string;
        dynamicModuleInfo.mAutoLoadType = i2;
        dynamicModuleInfo.mVersionCode = versionCode;
        dynamicModuleInfo.mTempPath = str;
        dynamicModuleInfo.mSuffix = FeatureInstallConfig.getSuffix(str);
        Log.d(TAG, "parseApk finish");
        return dynamicModuleInfo;
    }

    public static DynamicModuleInfo parseUnverifyApk(Context context, String str, String str2) {
        File unverifyApksDir = InstallStorageManager.getUnverifyApksDir(context);
        if (unverifyApksDir == null) {
            return null;
        }
        DynamicModuleInfo parseApk = parseApk(context, unverifyApksDir.getAbsolutePath() + File.separator + str);
        if (parseApk == null) {
            return parseApk;
        }
        parseApk.mExpectSignInfo = str2;
        return parseApk;
    }

    public synchronized void addDynamicFeatureState(DynamicFeatureState dynamicFeatureState) {
        this.mDynamicFeatureStates.put(dynamicFeatureState.featureName, dynamicFeatureState);
    }

    public void addInstalledModule(DynamicModuleInternal dynamicModuleInternal) {
        this.mDynamicModules.put(dynamicModuleInternal.mModuleInfo.mModuleName, dynamicModuleInternal);
    }

    public synchronized void delDynamicFeatureState(String str) {
        this.mDynamicFeatureStates.remove(str);
    }

    public synchronized int getDynamicFeatureState(String str) {
        DynamicFeatureState dynamicFeatureState;
        dynamicFeatureState = this.mDynamicFeatureStates.get(str);
        return dynamicFeatureState == null ? 0 : dynamicFeatureState.featureState;
    }

    public DynamicModuleInternal getDynamicModule(String str) {
        return this.mDynamicModules.get(str);
    }

    public HashMap<String, DynamicModuleInternal> getInstalledModules() {
        return this.mDynamicModules;
    }

    public boolean isMoudleInstalled(String str) {
        return getDynamicModule(str) != null;
    }
}
