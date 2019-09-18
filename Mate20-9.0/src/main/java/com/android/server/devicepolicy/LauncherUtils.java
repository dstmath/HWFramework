package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Binder;
import android.util.Flog;
import android.util.Log;
import com.android.server.security.antimal.HwAntiMalStatus;
import java.util.Arrays;
import java.util.List;

public class LauncherUtils {
    private static final String TAG = "LauncherUtils";

    public static void setDefaultLauncher(Context context, String packageName, String className) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
        int sz = resolveInfos.size();
        ComponentName[] set = new ComponentName[sz];
        int bestMatch = 0;
        int find = -1;
        for (int i = 0; i < sz; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (resolveInfo != null) {
                pm.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
                set[i] = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                if (resolveInfo.activityInfo.packageName.equals(packageName)) {
                    find = i;
                }
                if (resolveInfo.match > bestMatch) {
                    bestMatch = resolveInfo.match;
                }
            }
        }
        if (find != -1) {
            IntentFilter inf = new IntentFilter("android.intent.action.MAIN");
            inf.addAction("android.intent.action.MDM_SET_DEFAULT_LAUNCHER_FOR_ANTIMAL");
            inf.addCategory("android.intent.category.HOME");
            inf.addCategory("android.intent.category.DEFAULT");
            pm.addPreferredActivity(inf, bestMatch, set, set[find]);
        }
    }

    public static void clearDefaultLauncher(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
        int j = resolveInfos.size();
        for (int i = 0; i < j; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (resolveInfo != null) {
                pm.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
            }
        }
    }

    public static boolean checkLauncherPermisson(Context context, String launcherPackageName) {
        if (context == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        String[] pkgs = packageManager.getPackagesForUid(Binder.getCallingUid());
        if (pkgs == null || pkgs.length == 0) {
            return false;
        }
        try {
            PackageInfo info = packageManager.getPackageInfo(pkgs[0], 64);
            PackageInfo launcherInfo = packageManager.getPackageInfo(launcherPackageName, 64);
            if (info != null) {
                if (launcherInfo != null) {
                    if (new HwAntiMalStatus(context).isAllowedSetHomeActivityForAntiMal(launcherInfo, 0)) {
                        return true;
                    }
                    if (info.signatures != null) {
                        if (launcherInfo.signatures != null) {
                            for (Signature contains : info.signatures) {
                                if (Arrays.asList(launcherInfo.signatures).contains(contains)) {
                                    Flog.bdReport(context, 128, "{package:" + pkgs[0] + "-" + launcherPackageName + ",type:" + 10 + "}");
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                    Log.e(TAG, "info.signatures is null");
                    return false;
                }
            }
            Log.e(TAG, "info is null");
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
