package com.android.server.devicepolicy;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
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
import com.android.server.devicepolicy.plugins.DeviceSettingsPlugin;
import java.util.List;

public class LauncherUtils {
    private static final int INVALID_INDEX = -1;
    private static final String META_KEY_KEEP_ALIVE = "android.server.pm.KEEP_ALIVE";
    private static final String TAG = "LauncherUtils";

    private LauncherUtils() {
    }

    private static boolean isParameterValid(Context context, String packageName, String className, IPackageManager packageManager) {
        if (context == null || packageManager == null || !checkPkgAndClassNameValid(packageName, className)) {
            return false;
        }
        return true;
    }

    public static boolean setDefaultLauncher(Context context, String packageName, String className, IPackageManager packageManager, int userHandle) {
        PackageManager pm;
        String str = className;
        if (!isParameterValid(context, packageName, className, packageManager) || (pm = context.getPackageManager()) == null) {
            return false;
        }
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(getHomeIntent(), 0);
        int size = resolveInfos.size();
        ComponentName[] set = new ComponentName[size];
        int bestMatch = 0;
        int find = -1;
        int i = 0;
        while (i < size) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (!(resolveInfo == null || resolveInfo.activityInfo == null)) {
                String resolvePkgName = resolveInfo.activityInfo.packageName;
                String resolveClassName = resolveInfo.activityInfo.name;
                set[i] = new ComponentName(resolvePkgName, resolveClassName);
                String simpleClassName = resolveClassName.replace(resolvePkgName, DeviceSettingsPlugin.EMPTY_STRING);
                if (resolvePkgName.equals(packageName) && (resolveClassName.equals(str) || simpleClassName.equals(str))) {
                    find = i;
                }
                if (resolveInfo.match > bestMatch) {
                    bestMatch = resolveInfo.match;
                }
            }
            i++;
            str = className;
        }
        if (find == -1) {
            return false;
        }
        clearPreferredActivity(pm, resolveInfos);
        if (!addPreferredActivity(bestMatch, set, find, pm)) {
            return false;
        }
        if (checkAddPreferredActivityState(context, packageName, packageManager, userHandle)) {
            killNonDefaultLauncher(packageName, userHandle, pm);
            if (addPersistentPreferredActivityLocked(set[find], userHandle, packageManager)) {
                return true;
            }
            return false;
        }
        HwLog.e(TAG, "add preferred activity failed");
        return false;
    }

    private static void clearPreferredActivity(PackageManager pm, List<ResolveInfo> resolveInfos) {
        if (!(pm == null || resolveInfos == null)) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                if (!(resolveInfo == null || resolveInfo.activityInfo == null)) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        pm.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            }
        }
    }

    private static boolean checkAddPreferredActivityState(Context context, String packageName, IPackageManager packageManager, int userHandle) {
        return packageName.equals(getDefaultLauncherPackageName(context, packageManager, userHandle));
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

    private static String getDefaultLauncherPackageName(Context context, IPackageManager packageManager, int userId) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        try {
            ResolveInfo resolveInfo = packageManager.resolveIntent(intent, intent.resolveTypeIfNeeded(context.getContentResolver()), 65536, userId);
            if (resolveInfo == null || resolveInfo.activityInfo == null) {
                return DeviceSettingsPlugin.EMPTY_STRING;
            }
            return resolveInfo.activityInfo.packageName;
        } catch (RemoteException e) {
            HwLog.e(TAG, "RemoteException getActiveLauncherPackageName");
            return DeviceSettingsPlugin.EMPTY_STRING;
        }
    }

    private static void killNonDefaultLauncher(String defaultHome, int userId, PackageManager pm) {
        Throwable th;
        RemoteException ex;
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(getHomeIntent(), 128);
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am == null) {
            HwLog.e(TAG, "IActivityManager is null");
            return;
        }
        for (ResolveInfo info : resolveInfos) {
            String homePkg = info.activityInfo.packageName;
            HwLog.i(TAG, "resolveInfos home pkg = " + homePkg);
            Bundle metaData = info.activityInfo.metaData;
            boolean isKeepAlive = metaData != null ? metaData.getBoolean(META_KEY_KEEP_ALIVE, false) : false;
            if (!homePkg.equals(defaultHome)) {
                if (isKeepAlive) {
                    HwLog.i(TAG, "Skip killing package : " + homePkg);
                } else {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        HwLog.i(TAG, "killNonDefaultLauncher forceStopPackage : " + homePkg);
                        try {
                            am.forceStopPackage(homePkg, userId);
                        } catch (RemoteException e) {
                            ex = e;
                            try {
                                HwLog.e(TAG, "Failed to kill home package of " + homePkg);
                                Binder.restoreCallingIdentity(callingId);
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                    } catch (RemoteException e2) {
                        ex = e2;
                        HwLog.e(TAG, "Failed to kill home package of " + homePkg);
                        Binder.restoreCallingIdentity(callingId);
                    } catch (Throwable th3) {
                        th = th3;
                        Binder.restoreCallingIdentity(callingId);
                        throw th;
                    }
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
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

    private static Intent getHomeIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        return intent;
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
