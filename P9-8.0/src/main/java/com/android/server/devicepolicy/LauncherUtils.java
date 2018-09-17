package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

public class LauncherUtils {
    public static void setDefaultLauncher(Context context, String packageName, String className) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
        int sz = resolveInfos.size();
        ComponentName[] set = new ComponentName[sz];
        int bestMatch = 0;
        int find = -1;
        for (int i = 0; i < sz; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
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
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (resolveInfo != null) {
                pm.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
            }
        }
    }
}
