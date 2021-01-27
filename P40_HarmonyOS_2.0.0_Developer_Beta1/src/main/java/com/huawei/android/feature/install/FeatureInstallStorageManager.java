package com.huawei.android.feature.install;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;

public class FeatureInstallStorageManager {
    private static final String TAG = FeatureInstallStorageManager.class.getSimpleName();
    private File mLocalFile;
    private long mVersionCode = -1;

    public FeatureInstallStorageManager(Context context, String str) {
        this.mLocalFile = new File(context.getFilesDir().getAbsolutePath() + File.separator + str);
        try {
            this.mVersionCode = (long) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Get package info failed: Name not found.");
        }
    }

    public static File getDir(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                return file;
            }
            throw new IllegalArgumentException("File input must be directory when it exists.");
        } else if (file.mkdirs()) {
            return file;
        } else {
            String valueOf = String.valueOf(file.getAbsolutePath());
            if (TextUtils.isEmpty(valueOf)) {
                return file;
            }
            Log.d(TAG, "Unable to create directory: ".concat(String.valueOf(valueOf)));
            return null;
        }
    }

    public File getSplitCompatRootDir() {
        return getDir(new File(this.mLocalFile, "splitcompat"));
    }

    public final File getSplitDexDir(String str) {
        return getDir(new File(getDir(new File(getVersionCodeDir(), "dex")), str));
    }

    public File getVersionCodeDir() {
        return getDir(new File(getSplitCompatRootDir(), Long.toString(this.mVersionCode)));
    }
}
