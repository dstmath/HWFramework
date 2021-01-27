package com.huawei.android.feature.install.nonisolated;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;
import com.huawei.android.feature.compat.FeatureCompatVersionApiFactory;
import com.huawei.android.feature.install.BasePackageInfoManager;
import com.huawei.android.feature.install.FeatureApkInfo;
import com.huawei.android.feature.install.FeatureInstallStorageManager;
import com.huawei.android.feature.install.IDynamicFeatureInstaller;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleManager;
import com.huawei.android.feature.utils.CommonUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipFile;

public class HWFeatureCompat {
    private static final String ARM64_DIR = "arm64-v8a";
    private static final String ARMEABI_DIR = "armeabi-v7a";
    private static final String LIB_DIR = "lib";
    private static final String TAG = HWFeatureCompat.class.getSimpleName();
    private static final String ZIP_SEPARATOR = "!";
    private static FeatureInstallStorageManager sStorageManager;

    private static int installResource(Context context, String str) {
        AssetManager assets = context.getAssets();
        try {
            Method declaredMethod = assets.getClass().getDeclaredMethod("addAssetPath", String.class);
            declaredMethod.setAccessible(true);
            try {
                return ((Integer) declaredMethod.invoke(assets, str)).intValue();
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Invoke failed: Illegal access or invoke target exception.");
                return -1;
            }
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "Get reflect method failed: No such method.");
            return -2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x002c A[SYNTHETIC, Splitter:B:18:0x002c] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x003d A[SYNTHETIC, Splitter:B:25:0x003d] */
    private static boolean isDexApk(FeatureApkInfo featureApkInfo) {
        ZipFile zipFile;
        Throwable th;
        boolean z = false;
        try {
            zipFile = new ZipFile(featureApkInfo.getSplitApk());
            try {
                if (zipFile.getEntry("classes.dex") != null) {
                    z = true;
                }
                try {
                    zipFile.close();
                } catch (IOException e) {
                    Log.e(TAG, "Close zipFile failed.");
                }
            } catch (IOException e2) {
                try {
                    Log.e(TAG, "New zipFile failed.");
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "Close zipFile failed.");
                        }
                    }
                    return z;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipFile != null) {
                    }
                    throw th;
                }
            }
        } catch (IOException e4) {
            zipFile = null;
            Log.e(TAG, "New zipFile failed.");
            if (zipFile != null) {
            }
            return z;
        } catch (Throwable th3) {
            th = th3;
            zipFile = null;
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                    Log.e(TAG, "Close zipFile failed.");
                }
            }
            throw th;
        }
        return z;
    }

    private static synchronized int loadLocalSysSplitApks(Context context, Set<FeatureApkInfo> set) {
        int nativeSysInstalling;
        synchronized (HWFeatureCompat.class) {
            if (set == null) {
                Log.i(TAG, "Null loadedApks.");
                nativeSysInstalling = -26;
            } else {
                IDynamicFeatureInstaller createDynamicInstaller = FeatureCompatVersionApiFactory.createDynamicInstaller();
                nativeSysInstalling = nativeSysInstalling(context, set, createDynamicInstaller);
                if (nativeSysInstalling == 0) {
                    HashSet hashSet = new HashSet();
                    Iterator<FeatureApkInfo> it = set.iterator();
                    int i = nativeSysInstalling;
                    while (true) {
                        if (!it.hasNext()) {
                            Iterator it2 = hashSet.iterator();
                            while (it2.hasNext()) {
                                File file = (File) it2.next();
                                Log.d(TAG, "addAssetPath completed with " + file.getName() + ": " + installResource(context, file.getAbsolutePath()));
                            }
                            Iterator<FeatureApkInfo> it3 = set.iterator();
                            while (true) {
                                if (!it3.hasNext()) {
                                    nativeSysInstalling = i;
                                    break;
                                }
                                FeatureApkInfo next = it3.next();
                                if (!hashSet.contains(next.getSplitApk())) {
                                    Log.e(TAG, next.getSplitName() + " not installed.");
                                    nativeSysInstalling = -30;
                                    break;
                                }
                                updateInstallModules(context, next);
                                Log.d(TAG, next.getSplitName() + " installed successfully.");
                            }
                        } else {
                            FeatureApkInfo next2 = it.next();
                            ClassLoader classLoader = context.getClassLoader();
                            boolean isDexApk = isDexApk(next2);
                            i = createDynamicInstaller.dexInstall(classLoader, sStorageManager.getSplitDexDir(next2.getSplitName()), next2.getSplitApk());
                            if (isDexApk && i != 0) {
                                Log.e(TAG, "split " + next2.getSplitName() + " was not installed");
                                nativeSysInstalling = i;
                                break;
                            }
                            hashSet.add(next2.getSplitApk());
                        }
                    }
                }
            }
        }
        return nativeSysInstalling;
    }

    public static int loadSplits(Context context, Set<FeatureApkInfo> set) {
        if (Build.VERSION.SDK_INT < 21) {
            return -15;
        }
        sStorageManager = new FeatureInstallStorageManager(context, "hw");
        return loadLocalSysSplitApks(context, set);
    }

    private static int nativeSysInstalling(Context context, Set<FeatureApkInfo> set, IDynamicFeatureInstaller iDynamicFeatureInstaller) {
        ClassLoader classLoader = context.getClassLoader();
        HashSet hashSet = new HashSet();
        for (FeatureApkInfo featureApkInfo : set) {
            if (CommonUtils.is64Bit(context)) {
                hashSet.add(new File(featureApkInfo.getSplitApk().toString() + ZIP_SEPARATOR + File.separator + LIB_DIR + File.separator + ARM64_DIR));
            } else {
                hashSet.add(new File(featureApkInfo.getSplitApk().toString() + ZIP_SEPARATOR + File.separator + LIB_DIR + File.separator + ARMEABI_DIR));
            }
        }
        return iDynamicFeatureInstaller.nativeInstall(classLoader, hashSet);
    }

    private static void updateInstallModules(Context context, FeatureApkInfo featureApkInfo) {
        DynamicModuleInfo dynamicModuleInfo = new DynamicModuleInfo();
        dynamicModuleInfo.mModuleName = featureApkInfo.getSplitName();
        dynamicModuleInfo.mApkPath = featureApkInfo.getSplitApk().toString();
        dynamicModuleInfo.mIsIsolated = 0;
        dynamicModuleInfo.mVersionCode = BasePackageInfoManager.getInstance(context).getVersionCode();
        DynamicModuleManager.getInstance().addInstalledModule(new NonIsolatedDynamicModule(context, dynamicModuleInfo));
    }
}
