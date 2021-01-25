package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.securitydiagnose.HwSecurityDiagnoseManager;
import android.text.TextUtils;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import java.util.List;

public class LauncherUtils {
    private static final String TAG = "LauncherUtils";

    private LauncherUtils() {
    }

    private static boolean isParameterValid(Context context, String packageName, String className, IPackageManager packageManager) {
        if (context == null || packageManager == null || !checkPkgAndClassNameValid(packageName, className)) {
            return false;
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    public static boolean setDefaultLauncher(Context context, String packageName, String className, IPackageManager packageManager, int userHandle) {
        PackageManager pm;
        if (!isParameterValid(context, packageName, className, packageManager) || (pm = context.getPackageManager()) == null) {
            return false;
        }
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
        int size = resolveInfos.size();
        ComponentName[] set = new ComponentName[size];
        int find = -1;
        int bestMatch = 0;
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (resolveInfo != null) {
                if (resolveInfo.activityInfo != null) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        pm.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
                        Binder.restoreCallingIdentity(callingId);
                        String resolvePkgName = resolveInfo.activityInfo.packageName;
                        String resolveClassName = resolveInfo.activityInfo.name;
                        set[i] = new ComponentName(resolvePkgName, resolveClassName);
                        String simpleClassName = resolveClassName.replace(resolvePkgName, SettingsMDMPlugin.EMPTY_STRING);
                        if (resolvePkgName.equals(packageName) && (resolveClassName.equals(className) || simpleClassName.equals(className))) {
                            find = i;
                        }
                        if (resolveInfo.match > bestMatch) {
                            bestMatch = resolveInfo.match;
                        }
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingId);
                        throw th;
                    }
                }
            }
        }
        if (find == -1) {
            return false;
        }
        if (!addPreferredActivity(bestMatch, set, find, pm)) {
            return false;
        }
        if (addPersistentPreferredActivityLocked(set[find], userHandle, packageManager)) {
            return true;
        }
        return false;
    }

    private static boolean addPersistentPreferredActivityLocked(ComponentName componentName, int userHandle, IPackageManager packageManager) {
        if (componentName == null || packageManager == null) {
            return false;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.MAIN");
        filter.addCategory("android.intent.category.HOME");
        filter.addCategory("android.intent.category.DEFAULT");
        long callingId = Binder.clearCallingIdentity();
        try {
            packageManager.addPersistentPreferredActivity(filter, componentName, userHandle);
            packageManager.flushPackageRestrictionsAsUser(userHandle);
            HwLog.i(TAG, "add persistent preferred activity success.");
            return true;
        } catch (RemoteException e) {
            HwLog.e(TAG, "cannot add persistent preferred activity.");
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private static boolean addPreferredActivity(int bestMatch, ComponentName[] set, int find, PackageManager pm) {
        if (set == null || pm == null || set.length <= find) {
            return false;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.MAIN");
        filter.addCategory("android.intent.category.HOME");
        filter.addCategory("android.intent.category.DEFAULT");
        long callingId = Binder.clearCallingIdentity();
        pm.addPreferredActivity(filter, bestMatch, set, set[find]);
        Binder.restoreCallingIdentity(callingId);
        return true;
    }

    public static boolean clearDefaultLauncher(Context context, IPackageManager packageManager, int userHandle) {
        if (context == null || packageManager == null) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
        int size = resolveInfos.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (!(resolveInfo == null || resolveInfo.activityInfo == null)) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    pm.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
                    packageManager.clearPackagePersistentPreferredActivities(resolveInfo.activityInfo.packageName, userHandle);
                    packageManager.flushPackageRestrictionsAsUser(userHandle);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "cannot clear persistent preferred activity.");
                    return false;
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
        return true;
    }

    public static boolean checkPkgAndClassNameValid(String packageName, String className) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
            return false;
        }
        return true;
    }

    public static boolean checkLauncherPermisson(String launcherPackageName) {
        HwSecurityDiagnoseManager sdm = HwSecurityDiagnoseManager.getInstance();
        if (sdm == null) {
            return true;
        }
        Bundle params = new Bundle();
        params.putString("pkg", launcherPackageName);
        params.putInt("uid", Binder.getCallingUid());
        params.putInt("src", HwSecurityDiagnoseManager.AntiMalProtectLauncherType.HW_DPM.ordinal());
        if (sdm.getAntimalProtectionPolicy(HwSecurityDiagnoseManager.AntiMalProtectType.LAUNCHER.ordinal(), params) != 1) {
            return true;
        }
        return false;
    }
}
