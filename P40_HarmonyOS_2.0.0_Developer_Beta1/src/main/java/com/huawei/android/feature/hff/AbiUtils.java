package com.huawei.android.feature.hff;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import java.io.File;

public class AbiUtils {
    private static final String ARM64 = "arm64-v8a";
    private static final String ARMEABI = "armeabi";
    public static final int ARMEABI_TYPE = 1;
    private static final String ARMEABI_V7A = "armeabi-v7a";
    public static final int ARMEABI_V7A_TYPE = 0;
    private static final String LIB_DIR = "lib";
    private static final String TAG = AbiUtils.class.getSimpleName();
    private static final String ZIP_SEPARATOR = "!";

    public static String getApkNativePath(Context context, String str, int i) {
        if (Build.VERSION.SDK_INT > 23) {
            return is64Bit(context) ? str + ZIP_SEPARATOR + File.separator + LIB_DIR + File.separator + ARM64 : i == 1 ? str + ZIP_SEPARATOR + File.separator + LIB_DIR + File.separator + ARMEABI : str + ZIP_SEPARATOR + File.separator + LIB_DIR + File.separator + ARMEABI_V7A;
        }
        return null;
    }

    public static boolean is64Bit(Context context) {
        if (context == null) {
            Log.e(TAG, "Null context, please check it.");
            return false;
        }
        if (context.getApplicationContext() != null) {
            context = context.getApplicationContext();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return Process.is64Bit();
        }
        if (Build.VERSION.SDK_INT < 21) {
            return false;
        }
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 128).nativeLibraryDir.contains("64");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Get application info failed: name not found.");
            return false;
        }
    }
}
