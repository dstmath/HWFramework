package com.huawei.android.feature.install;

import android.content.Context;
import android.util.Log;
import java.io.File;

public class InstallStorageManager {
    private static final String BASE_DIR = "featureinstall";
    private static final String DEX_DIR = "feature_dex";
    private static final String ISOLATED_DIR = "isolated";
    public static final String ISOLATED_UPDATE_DIR = "update-apk";
    public static final String LIBS_DIR = "feature_libs";
    private static final String NONISOLATED_DIR = "non-isolated";
    private static final String NONISOLATED_VERIFIED_DIR = "verify-apks";
    private static final String TAG = InstallStorageManager.class.getSimpleName();
    private static final String UNVERIFY_APKS_DIR = "unverify-apks";
    private static volatile File sBaseFile;

    public static void deleteFile(File file) {
        File[] listFiles;
        if (file != null) {
            if (file.isDirectory() && (listFiles = file.listFiles()) != null) {
                for (File file2 : listFiles) {
                    deleteFile(file2);
                }
            }
            if (file.exists()) {
                Log.d(TAG, "delete success = ".concat(String.valueOf(file.delete())));
            }
        }
    }

    public static File getBaseDir(Context context) {
        if (sBaseFile == null) {
            sBaseFile = new File(context.getFilesDir(), BASE_DIR);
        }
        return getDir(sBaseFile);
    }

    public static File getDir(File file) {
        if (file == null) {
            return null;
        }
        if (file.exists()) {
            if (!file.isDirectory()) {
                return null;
            }
            return file;
        } else if (!file.mkdirs()) {
            return null;
        } else {
            return file;
        }
    }

    public static File getIsolateUpdateDir(Context context, String str) {
        File isolatedModuleDir = getIsolatedModuleDir(context, str);
        if (isolatedModuleDir == null) {
            return null;
        }
        return getDir(new File(isolatedModuleDir, ISOLATED_UPDATE_DIR));
    }

    public static File getIsolatedDir(Context context) {
        File baseDir = getBaseDir(context);
        if (baseDir == null) {
            return null;
        }
        return getDir(new File(baseDir, ISOLATED_DIR));
    }

    public static File getIsolatedModuleDexDir(Context context, String str) {
        File isolatedModuleDir = getIsolatedModuleDir(context, str);
        if (isolatedModuleDir == null) {
            return null;
        }
        return getDir(new File(isolatedModuleDir, DEX_DIR));
    }

    public static File getIsolatedModuleDir(Context context, String str) {
        File isolatedDir = getIsolatedDir(context);
        if (isolatedDir == null) {
            return null;
        }
        return getDir(new File(isolatedDir, str));
    }

    public static File getIsolatedModuleNativeDir(Context context, String str) {
        File isolatedModuleDir = getIsolatedModuleDir(context, str);
        if (isolatedModuleDir == null) {
            return null;
        }
        return getDir(new File(isolatedModuleDir, LIBS_DIR));
    }

    public static File getNonIsolatedDexDir(Context context, long j) {
        File nonIsolatedVersionCodeDir = getNonIsolatedVersionCodeDir(context, j);
        if (nonIsolatedVersionCodeDir == null) {
            return null;
        }
        return getDir(new File(nonIsolatedVersionCodeDir, DEX_DIR));
    }

    public static File getNonIsolatedDir(Context context) {
        File baseDir = getBaseDir(context);
        if (baseDir == null) {
            return null;
        }
        return getDir(new File(baseDir, NONISOLATED_DIR));
    }

    public static File getNonIsolatedFeatureDexDir(Context context, long j, String str) {
        File nonIsolatedDexDir = getNonIsolatedDexDir(context, j);
        if (nonIsolatedDexDir == null) {
            return null;
        }
        return getDir(new File(nonIsolatedDexDir, str));
    }

    public static File getNonIsolatedFeatureLibsDir(Context context, long j, String str) {
        File nonIsolatedLibsDir = getNonIsolatedLibsDir(context, j);
        if (nonIsolatedLibsDir == null) {
            return null;
        }
        return getDir(new File(nonIsolatedLibsDir, str));
    }

    public static File getNonIsolatedLibsDir(Context context, long j) {
        File nonIsolatedVersionCodeDir = getNonIsolatedVersionCodeDir(context, j);
        if (nonIsolatedVersionCodeDir == null) {
            return null;
        }
        return getDir(new File(nonIsolatedVersionCodeDir, LIBS_DIR));
    }

    public static File getNonIsolatedVerifyDir(Context context, long j) {
        File nonIsolatedVersionCodeDir = getNonIsolatedVersionCodeDir(context, j);
        if (nonIsolatedVersionCodeDir == null) {
            return null;
        }
        return getDir(new File(nonIsolatedVersionCodeDir, NONISOLATED_VERIFIED_DIR));
    }

    public static File getNonIsolatedVersionCodeDir(Context context, long j) {
        File nonIsolatedDir = getNonIsolatedDir(context);
        if (nonIsolatedDir == null) {
            return null;
        }
        return getDir(new File(nonIsolatedDir, String.valueOf(j)));
    }

    public static File getUnverifyApksDir(Context context) {
        File baseDir = getBaseDir(context);
        if (baseDir == null) {
            return null;
        }
        return getDir(new File(baseDir, UNVERIFY_APKS_DIR));
    }

    public static void initBaseDir(File file) {
        sBaseFile = new File(file, BASE_DIR);
    }
}
