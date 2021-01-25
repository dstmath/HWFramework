package com.huawei.android.feature.compat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.feature.hff.HFFInstallManager;
import com.huawei.android.feature.install.FeatureDownloadInfo;
import com.huawei.android.feature.install.InstallBgExecutor;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.config.FeatureInstallConfig;
import com.huawei.android.feature.install.isolated.IsolatedFeatureInstaller;
import com.huawei.android.feature.install.nonisolated.NonIsolatedFeatureInstaller;
import com.huawei.android.feature.module.DynamicFeatureState;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class InstallCompat {
    private static final String TAG = InstallCompat.class.getSimpleName();

    public static void asyncinstall(Context context) {
        InstallBgExecutor.getExecutor().execute(new c(context));
    }

    private static Set<FeatureDownloadInfo> getExistApksInfo(Context context, long j) {
        File[] listFiles;
        HashSet hashSet = new HashSet();
        File nonIsolatedVerifyDir = InstallStorageManager.getNonIsolatedVerifyDir(context, j);
        if (!(nonIsolatedVerifyDir == null || (listFiles = nonIsolatedVerifyDir.listFiles()) == null)) {
            for (File file : listFiles) {
                if (file.isFile() && FeatureInstallConfig.isFileEndWithConfig(file)) {
                    hashSet.add(new FeatureDownloadInfo(file, file.getName().substring(0, file.getName().length() - 4)));
                }
            }
        }
        return hashSet;
    }

    private static Set<FeatureDownloadInfo> getInstalledFeature(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long j = (long) packageInfo.versionCode;
            ArrayList arrayList = packageInfo.splitNames == null ? new ArrayList() : Arrays.asList(packageInfo.splitNames);
            Set<FeatureDownloadInfo> existApksInfo = getExistApksInfo(context, j);
            Iterator<FeatureDownloadInfo> it = existApksInfo.iterator();
            HashSet hashSet = new HashSet();
            while (it.hasNext()) {
                String str = it.next().mFileName;
                if (arrayList.contains(str)) {
                    hashSet.add(str);
                    it.remove();
                }
            }
            if (!hashSet.isEmpty()) {
                InstallBgExecutor.getExecutor().execute(new f(hashSet, context, j));
            }
            return existApksInfo;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public static int install(Context context) {
        int installIsolatedIfNeed = installIsolatedIfNeed(context, true);
        return (installIsolatedIfNeed == 0 && Build.VERSION.SDK_INT >= 21) ? installNonIsolated(context) : installIsolatedIfNeed;
    }

    public static int installHFF(Context context, String str, HashMap<String, Integer> hashMap) {
        if (!context.getPackageCodePath().startsWith(File.separator + "system")) {
            Log.w(TAG, "Not system preset app, cannot install HFF splits.");
            return -3;
        }
        int installIsolated = HFFInstallManager.installIsolated(context, HFFInstallManager.getMetaSplitsInfo(context, str), hashMap);
        if (installIsolated != 0) {
            Log.e(TAG, "Install isolated feature failed, errCode:".concat(String.valueOf(installIsolated)));
            return installIsolated;
        }
        Log.i(TAG, "Install isolated feature successfully.");
        return installIsolated;
    }

    /* access modifiers changed from: private */
    public static int installIsolatedIfNeed(Context context, boolean z) {
        Log.d(TAG, "installIsolated beginning");
        File isolatedDir = InstallStorageManager.getIsolatedDir(context);
        if (isolatedDir == null) {
            return 0;
        }
        File[] listFiles = isolatedDir.listFiles();
        if (listFiles == null) {
            return 0;
        }
        int i = 0;
        for (File file : listFiles) {
            updateApkIfNeed(file);
            File[] listFiles2 = file.listFiles();
            if (listFiles2 != null) {
                for (File file2 : listFiles2) {
                    if (FeatureInstallConfig.isFileEndWithConfig(file2)) {
                        DynamicModuleInfo parseApk = DynamicModuleManager.parseApk(context, file2.getAbsolutePath());
                        if (parseApk == null) {
                            InstallBgExecutor.getExecutor().execute(new d(file2));
                        } else if (DynamicModuleManager.getInstance().getDynamicFeatureState(parseApk.mModuleName) != 0) {
                            i = -25;
                            Log.e(TAG, "use install api before installcompt");
                        } else {
                            DynamicModuleManager.getInstance().addDynamicFeatureState(new DynamicFeatureState(parseApk.mModuleName, 4));
                            new IsolatedFeatureInstaller().installFeatureFromVerify(context, parseApk);
                            if (parseApk.mAutoLoadType == 1) {
                                int install = DynamicModuleManager.getInstance().getDynamicModule(parseApk.mModuleName).install(z);
                                if (install != 0) {
                                    DynamicModuleManager.getInstance().delDynamicFeatureState(parseApk.mModuleName);
                                    InstallBgExecutor.getExecutor().execute(new e(context, parseApk));
                                    i = install;
                                } else {
                                    DynamicModuleManager.getInstance().addDynamicFeatureState(new DynamicFeatureState(parseApk.mModuleName, 5));
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "installIsolated finish, errcode:".concat(String.valueOf(i)));
        return i;
    }

    /* access modifiers changed from: private */
    public static int installNonIsolated(Context context) {
        int i;
        Set<FeatureDownloadInfo> installedFeature = getInstalledFeature(context);
        if (installedFeature == null) {
            return 0;
        }
        int i2 = 0;
        for (FeatureDownloadInfo featureDownloadInfo : installedFeature) {
            DynamicModuleInfo parseApk = DynamicModuleManager.parseApk(context, featureDownloadInfo.mFile.getAbsolutePath());
            if (parseApk != null) {
                new NonIsolatedFeatureInstaller().installFeatureFromVerify(context, parseApk);
                if (parseApk.mAutoLoadType != 1 || (i = DynamicModuleManager.getInstance().getDynamicModule(parseApk.mModuleName).install()) == 0) {
                    i = i2;
                }
                i2 = i;
            }
        }
        return i2;
    }

    public static int installNotOverride(Context context) {
        int installIsolatedIfNeed = installIsolatedIfNeed(context, false);
        return (installIsolatedIfNeed == 0 && Build.VERSION.SDK_INT >= 21) ? installNonIsolated(context) : installIsolatedIfNeed;
    }

    private static void updateApkIfNeed(File file) {
        File file2 = new File(file, InstallStorageManager.ISOLATED_UPDATE_DIR);
        if (file2.exists()) {
            Iterator<String> it = FeatureInstallConfig.SUPPORT.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String next = it.next();
                File file3 = new File(file2, file.getName() + next);
                if (file3.exists()) {
                    File[] listFiles = file.listFiles();
                    if (listFiles != null) {
                        for (File file4 : listFiles) {
                            if (!TextUtils.equals(file4.getName(), InstallStorageManager.ISOLATED_UPDATE_DIR)) {
                                InstallStorageManager.deleteFile(file4);
                            }
                        }
                        Log.d(TAG, "isRenameSuccess : ".concat(String.valueOf(file3.renameTo(new File(file, file.getName() + next)))));
                    }
                }
            }
            InstallStorageManager.deleteFile(file2);
        }
    }
}
