package com.huawei.pluginmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PluginManager {
    private static final Object LOCK = new Object();
    private static final String TAG = "PluginManager";
    private static final int VERSIONCODE = 10;
    private static AtomicBoolean sInit = new AtomicBoolean(false);
    private static int sPluginSdkVersionCode;

    private PluginManager() {
    }

    private static ApplicationInfo getApplicationInfo(Context context) {
        if (context == null) {
            Log.w(TAG, "getApplicationInfo context is null");
            return null;
        }
        String packageName = context.getPackageName();
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo NameNotFoundException for " + packageName);
            return null;
        }
    }

    public static boolean init(Context context) {
        if (context == null) {
            Log.e(TAG, "init fail context is null");
            return false;
        }
        synchronized (LOCK) {
            String packageName = context.getPackageName();
            if (sInit.get()) {
                Log.i(TAG, "already init for " + packageName);
                return true;
            }
            try {
                sPluginSdkVersionCode = ((int) context.getPackageManager().getPackageInfo(packageName, 0).getLongVersionCode()) / 10000;
                sInit.set(true);
                Log.i(TAG, "init success for " + packageName);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "init fail NameNotFoundException for " + packageName);
                return false;
            }
        }
    }

    public static int getVersion() {
        return 10;
    }

    public static int getPluginSdkVersionCode() {
        synchronized (LOCK) {
            if (!sInit.get()) {
                Log.e(TAG, "getPluginSdkVersionCode it should init first");
                return -1;
            }
            return sPluginSdkVersionCode;
        }
    }

    static int getPluginSdkVersionCode(Context context) {
        init(context);
        return getPluginSdkVersionCode();
    }

    public static boolean hasPlugin(Context context) {
        if (context == null) {
            Log.e(TAG, "hasPlugin context is null");
            return false;
        }
        ApplicationInfo ai = getApplicationInfo(context);
        if (ai != null) {
            return ai.hasPlugin();
        }
        Log.e(TAG, "hasPlugin ApplicationInfo is null for " + context.getPackageName());
        return false;
    }

    public static String[] getPluginNames(Context context) {
        if (context == null) {
            Log.e(TAG, "getPluginNames context is null");
            return new String[0];
        }
        ApplicationInfo ai = getApplicationInfo(context);
        if (ai == null) {
            Log.e(TAG, "getPluginNames ApplicationInfo is null for " + context.getPackageName());
            return new String[0];
        }
        int[] hwSplitFlags = ai.hwSplitFlags;
        if (hwSplitFlags == null || hwSplitFlags.length == 0) {
            Log.e(TAG, "getPluginNames hwSplitFlags is empty for " + ai.packageName);
            return new String[0];
        }
        Log.i(TAG, "getPluginNames hwSplitFlags=" + Arrays.asList(hwSplitFlags));
        String[] splitNames = ai.splitNames;
        if (splitNames == null) {
            Log.e(TAG, "getPluginNames splitNames is null for " + ai.packageName);
            return new String[0];
        }
        Log.i(TAG, "getPluginNames splitNames=" + Arrays.asList(splitNames));
        if (splitNames.length != hwSplitFlags.length) {
            Log.e(TAG, "getPluginNames splitNames length not equals with hwSplitFlags for " + ai.packageName);
            return new String[0];
        }
        List<String> pluginNames = new ArrayList<>(0);
        int index = 0;
        for (int flag : hwSplitFlags) {
            if ((Integer.MIN_VALUE & flag) != 0) {
                pluginNames.add(splitNames[index]);
            }
            index++;
        }
        return (String[]) pluginNames.toArray(new String[pluginNames.size()]);
    }
}
