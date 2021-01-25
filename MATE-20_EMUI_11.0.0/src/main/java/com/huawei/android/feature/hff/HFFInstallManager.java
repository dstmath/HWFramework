package com.huawei.android.feature.hff;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.huawei.android.feature.install.config.FeatureInstallConfig;
import com.huawei.android.feature.install.isolated.IsolatedDynamicModule;
import com.huawei.android.feature.module.DynamicFeatureState;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HFFInstallManager {
    public static final String HFF_PACKAGE_NAME = "com.huawei.hff";
    private static final String PRE_SPLITS = "presplits";
    private static final String SPLIT_SEPARATOR = ",";
    private static final String TAG = HFFInstallManager.class.getSimpleName();

    private static Set<SplitsInfo> addAbiType(Set<SplitsInfo> set, HashMap<String, Integer> hashMap) {
        HashSet hashSet = new HashSet();
        for (SplitsInfo splitsInfo : set) {
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                if (splitsInfo.splitName.equals(entry.getKey())) {
                    hashSet.add(new SplitsInfo(splitsInfo.splitName, splitsInfo.splitPath, splitsInfo.splitVersion, entry.getValue().intValue()));
                }
            }
        }
        return hashSet;
    }

    public static Set<SplitsInfo> getMetaSplitsInfo(Context context, String str) {
        ApplicationInfo applicationInfo = null;
        HashSet hashSet = new HashSet();
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getMetaSplits: cannot find the package info.");
        }
        if (applicationInfo == null) {
            return hashSet;
        }
        String string = applicationInfo.metaData.getString(PRE_SPLITS);
        if (string == null) {
            Log.w(TAG, "Get meta-data:presplits failed.");
            return hashSet;
        }
        String[] split = string.split(SPLIT_SEPARATOR);
        Set<SplitsInfo> packageSplitsInfo = getPackageSplitsInfo(context, str);
        if (split.length == 0 || packageSplitsInfo.isEmpty()) {
            return hashSet;
        }
        for (SplitsInfo splitsInfo : packageSplitsInfo) {
            for (String str2 : split) {
                if (splitsInfo.splitName.equals(str2)) {
                    hashSet.add(splitsInfo);
                }
            }
        }
        return hashSet;
    }

    public static Set<SplitsInfo> getPackageSplitsInfo(Context context, String str) {
        HashSet hashSet = new HashSet();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(str, 128);
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(str, 128);
            if (packageInfo.splitNames == null || applicationInfo.splitSourceDirs == null) {
                Log.e(TAG, "splitNames or splitSourceDirs is null.");
                return hashSet;
            }
            for (int i = 0; i < packageInfo.splitNames.length; i++) {
                if (Build.VERSION.SDK_INT >= 22) {
                    hashSet.add(new SplitsInfo(packageInfo.splitNames[i], applicationInfo.splitSourceDirs[i], packageInfo.splitRevisionCodes[i]));
                } else {
                    hashSet.add(new SplitsInfo(packageInfo.splitNames[i], applicationInfo.splitSourceDirs[i], 0));
                }
            }
            return hashSet;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getSourceDir: cannot find the package info.");
        }
    }

    private static boolean installFeatureFromHFF(Context context, SplitsInfo splitsInfo) {
        Log.d(TAG, "install HFF feature:" + splitsInfo.splitName + " installing");
        if (!new File(splitsInfo.splitPath).exists()) {
            Log.w(TAG, "splitPath:" + splitsInfo.splitPath + "not exist or cannot access.");
            return false;
        }
        DynamicModuleInfo dynamicModuleInfo = new DynamicModuleInfo();
        dynamicModuleInfo.mModuleName = splitsInfo.splitName;
        dynamicModuleInfo.mIsIsolated = 1;
        dynamicModuleInfo.mVersionCode = (long) splitsInfo.splitVersion;
        dynamicModuleInfo.mSignatureVerifyType = 0;
        dynamicModuleInfo.mApkPath = splitsInfo.splitPath;
        dynamicModuleInfo.mDexDir = splitsInfo.splitPath;
        dynamicModuleInfo.mNativeLibDir = AbiUtils.getApkNativePath(context, splitsInfo.splitPath, splitsInfo.armeabiType);
        dynamicModuleInfo.mSuffix = FeatureInstallConfig.getSuffix(splitsInfo.splitPath);
        DynamicModuleManager.getInstance().addInstalledModule(new IsolatedDynamicModule(context, dynamicModuleInfo));
        Log.d(TAG, "install HFF feature: " + splitsInfo.splitName + " installed");
        return true;
    }

    public static int installIsolated(Context context, Set<SplitsInfo> set, HashMap<String, Integer> hashMap) {
        Log.d(TAG, "install isolated features begin.");
        int i = 0;
        for (SplitsInfo splitsInfo : addAbiType(set, hashMap)) {
            if (DynamicModuleManager.getInstance().getDynamicFeatureState(splitsInfo.splitName) != 0) {
                Log.e(TAG, "use install api before installcompt");
                i = -25;
            } else {
                DynamicModuleManager.getInstance().addDynamicFeatureState(new DynamicFeatureState(splitsInfo.splitName, 4));
                if (!installFeatureFromHFF(context, splitsInfo)) {
                    Log.w(TAG, "Install feature:" + splitsInfo.splitName + " from HFF failed.");
                } else {
                    int install = DynamicModuleManager.getInstance().getDynamicModule(splitsInfo.splitName).install(false);
                    if (install != 0) {
                        DynamicModuleManager.getInstance().delDynamicFeatureState(splitsInfo.splitName);
                        i = install;
                    } else {
                        DynamicModuleManager.getInstance().addDynamicFeatureState(new DynamicFeatureState(splitsInfo.splitName, 5));
                        Log.d(TAG, "Install HFF feature:" + splitsInfo.splitName + " successfully.");
                    }
                }
            }
        }
        Log.d(TAG, "install Isolated finish, errcode:".concat(String.valueOf(i)));
        return i;
    }
}
