package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.Resources;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.SystemConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class CarrierAppUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "CarrierAppUtils";

    private CarrierAppUtils() {
    }

    public static synchronized void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, TelephonyManager telephonyManager, ContentResolver contentResolver, int userId) {
        synchronized (CarrierAppUtils.class) {
            SystemConfig config = SystemConfig.getInstance();
            disableCarrierAppsUntilPrivileged(callingPackage, packageManager, telephonyManager, contentResolver, userId, config.getDisabledUntilUsedPreinstalledCarrierApps(), config.getDisabledUntilUsedPreinstalledCarrierAssociatedApps());
        }
    }

    public static synchronized void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, ContentResolver contentResolver, int userId) {
        synchronized (CarrierAppUtils.class) {
            SystemConfig config = SystemConfig.getInstance();
            disableCarrierAppsUntilPrivileged(callingPackage, packageManager, null, contentResolver, userId, config.getDisabledUntilUsedPreinstalledCarrierApps(), config.getDisabledUntilUsedPreinstalledCarrierAssociatedApps());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a7  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x018d A[Catch:{ RemoteException -> 0x0233 }] */
    @VisibleForTesting
    public static void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, TelephonyManager telephonyManager, ContentResolver contentResolver, int userId, ArraySet<String> systemCarrierAppsDisabledUntilUsed, ArrayMap<String, List<String>> systemCarrierAssociatedAppsDisabledUntilUsed) {
        boolean hasPrivileges;
        List<ApplicationInfo> associatedAppList;
        List<ApplicationInfo> candidates;
        List<ApplicationInfo> candidates2;
        boolean z;
        String str;
        ApplicationInfo ai;
        String str2;
        String str3;
        char c;
        String[] restrictedCarrierApps;
        TelephonyManager telephonyManager2 = telephonyManager;
        List<ApplicationInfo> candidates3 = getDefaultCarrierAppCandidatesHelper(packageManager, userId, systemCarrierAppsDisabledUntilUsed);
        if (candidates3 == null) {
            return;
        }
        if (!candidates3.isEmpty()) {
            Map<String, List<ApplicationInfo>> associatedApps = getDefaultCarrierAssociatedAppsHelper(packageManager, userId, systemCarrierAssociatedAppsDisabledUntilUsed);
            List<ApplicationInfo> enabledCarrierPackages = new ArrayList<>();
            boolean z2 = true;
            boolean hasRunOnce = Settings.Secure.getIntForUser(contentResolver, Settings.Secure.CARRIER_APPS_HANDLED, 0, userId) == 1;
            try {
                for (ApplicationInfo ai2 : candidates3) {
                    String packageName = ai2.packageName;
                    String[] restrictedCarrierApps2 = Resources.getSystem().getStringArray(R.array.config_restrictedPreinstalledCarrierApps);
                    if (telephonyManager2 != null) {
                        try {
                            if (telephonyManager2.checkCarrierPrivilegesForPackageAnyPhone(packageName) == z2 && !ArrayUtils.contains(restrictedCarrierApps2, packageName)) {
                                hasPrivileges = z2;
                                packageManager.setSystemAppHiddenUntilInstalled(packageName, z2);
                                associatedAppList = associatedApps.get(packageName);
                                if (associatedAppList != null) {
                                    for (Iterator<ApplicationInfo> it = associatedAppList.iterator(); it.hasNext(); it = it) {
                                        packageManager.setSystemAppHiddenUntilInstalled(it.next().packageName, true);
                                    }
                                }
                                if (!hasPrivileges) {
                                    if (!ai2.isUpdatedSystemApp()) {
                                        if (ai2.enabledSetting != 0) {
                                            restrictedCarrierApps = restrictedCarrierApps2;
                                            if (!(ai2.enabledSetting == 4 || (ai2.flags & 8388608) == 0)) {
                                                ai = ai2;
                                                str = "): ENABLED for user ";
                                                candidates = candidates3;
                                                str2 = "Update associated state(";
                                                candidates2 = enabledCarrierPackages;
                                            }
                                        } else {
                                            restrictedCarrierApps = restrictedCarrierApps2;
                                        }
                                        Slog.i(TAG, "Update state(" + packageName + "): ENABLED for user " + userId);
                                        packageManager.setSystemAppInstallState(packageName, true, userId);
                                        ai = ai2;
                                        str2 = "Update associated state(";
                                        str = "): ENABLED for user ";
                                        candidates = candidates3;
                                        candidates2 = enabledCarrierPackages;
                                        try {
                                            packageManager.setApplicationEnabledSetting(packageName, 1, 1, userId, callingPackage);
                                        } catch (RemoteException e) {
                                            e = e;
                                            Slog.w(TAG, "Could not reach PackageManager", e);
                                        }
                                    } else {
                                        ai = ai2;
                                        str = "): ENABLED for user ";
                                        candidates = candidates3;
                                        str2 = "Update associated state(";
                                        candidates2 = enabledCarrierPackages;
                                    }
                                    if (associatedAppList != null) {
                                        for (ApplicationInfo associatedApp : associatedAppList) {
                                            if (associatedApp.enabledSetting != 0) {
                                                c = 4;
                                                if (associatedApp.enabledSetting != 4) {
                                                    if ((associatedApp.flags & 8388608) != 0) {
                                                        str3 = str;
                                                        str = str3;
                                                    }
                                                }
                                            } else {
                                                c = 4;
                                            }
                                            Slog.i(TAG, str2 + associatedApp.packageName + str + userId);
                                            packageManager.setSystemAppInstallState(associatedApp.packageName, true, userId);
                                            str3 = str;
                                            packageManager.setApplicationEnabledSetting(associatedApp.packageName, 1, 1, userId, callingPackage);
                                            str = str3;
                                        }
                                    }
                                    candidates2.add(ai.packageName);
                                    z = false;
                                } else {
                                    candidates = candidates3;
                                    candidates2 = enabledCarrierPackages;
                                    if (ai2.isUpdatedSystemApp() || ai2.enabledSetting != 0 || (ai2.flags & 8388608) == 0) {
                                        z = false;
                                    } else {
                                        Slog.i(TAG, "Update state(" + packageName + "): DISABLED_UNTIL_USED for user " + userId);
                                        z = false;
                                        packageManager.setSystemAppInstallState(packageName, false, userId);
                                    }
                                    if (!hasRunOnce && associatedAppList != null) {
                                        for (ApplicationInfo associatedApp2 : associatedAppList) {
                                            if (associatedApp2.enabledSetting == 0 && (associatedApp2.flags & 8388608) != 0) {
                                                Slog.i(TAG, "Update associated state(" + associatedApp2.packageName + "): DISABLED_UNTIL_USED for user " + userId);
                                                packageManager.setSystemAppInstallState(associatedApp2.packageName, z, userId);
                                            }
                                        }
                                    }
                                }
                                telephonyManager2 = telephonyManager;
                                enabledCarrierPackages = candidates2;
                                candidates3 = candidates;
                                z2 = true;
                            }
                        } catch (RemoteException e2) {
                            e = e2;
                            Slog.w(TAG, "Could not reach PackageManager", e);
                        }
                    }
                    hasPrivileges = false;
                    packageManager.setSystemAppHiddenUntilInstalled(packageName, z2);
                    associatedAppList = associatedApps.get(packageName);
                    if (associatedAppList != null) {
                    }
                    if (!hasPrivileges) {
                    }
                    telephonyManager2 = telephonyManager;
                    enabledCarrierPackages = candidates2;
                    candidates3 = candidates;
                    z2 = true;
                }
                if (!hasRunOnce) {
                    Settings.Secure.putIntForUser(contentResolver, Settings.Secure.CARRIER_APPS_HANDLED, 1, userId);
                }
                if (!enabledCarrierPackages.isEmpty()) {
                    String[] packageNames = new String[enabledCarrierPackages.size()];
                    enabledCarrierPackages.toArray(packageNames);
                    packageManager.grantDefaultPermissionsToEnabledCarrierApps(packageNames, userId);
                }
            } catch (RemoteException e3) {
                e = e3;
                Slog.w(TAG, "Could not reach PackageManager", e);
            }
        }
    }

    public static List<ApplicationInfo> getDefaultCarrierApps(IPackageManager packageManager, TelephonyManager telephonyManager, int userId) {
        List<ApplicationInfo> candidates = getDefaultCarrierAppCandidates(packageManager, userId);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        for (int i = candidates.size() - 1; i >= 0; i--) {
            if (!(telephonyManager.checkCarrierPrivilegesForPackageAnyPhone(candidates.get(i).packageName) == 1)) {
                candidates.remove(i);
            }
        }
        return candidates;
    }

    public static List<ApplicationInfo> getDefaultCarrierAppCandidates(IPackageManager packageManager, int userId) {
        return getDefaultCarrierAppCandidatesHelper(packageManager, userId, SystemConfig.getInstance().getDisabledUntilUsedPreinstalledCarrierApps());
    }

    private static List<ApplicationInfo> getDefaultCarrierAppCandidatesHelper(IPackageManager packageManager, int userId, ArraySet<String> systemCarrierAppsDisabledUntilUsed) {
        int size;
        if (systemCarrierAppsDisabledUntilUsed == null || (size = systemCarrierAppsDisabledUntilUsed.size()) == 0) {
            return null;
        }
        List<ApplicationInfo> apps = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ApplicationInfo ai = getApplicationInfoIfSystemApp(packageManager, userId, systemCarrierAppsDisabledUntilUsed.valueAt(i));
            if (ai != null) {
                apps.add(ai);
            }
        }
        return apps;
    }

    private static Map<String, List<ApplicationInfo>> getDefaultCarrierAssociatedAppsHelper(IPackageManager packageManager, int userId, ArrayMap<String, List<String>> systemCarrierAssociatedAppsDisabledUntilUsed) {
        int size = systemCarrierAssociatedAppsDisabledUntilUsed.size();
        Map<String, List<ApplicationInfo>> associatedApps = new ArrayMap<>(size);
        for (int i = 0; i < size; i++) {
            String carrierAppPackage = systemCarrierAssociatedAppsDisabledUntilUsed.keyAt(i);
            List<String> associatedAppPackages = systemCarrierAssociatedAppsDisabledUntilUsed.valueAt(i);
            for (int j = 0; j < associatedAppPackages.size(); j++) {
                ApplicationInfo ai = getApplicationInfoIfSystemApp(packageManager, userId, associatedAppPackages.get(j));
                if (ai != null && !ai.isUpdatedSystemApp()) {
                    List<ApplicationInfo> appList = associatedApps.get(carrierAppPackage);
                    if (appList == null) {
                        appList = new ArrayList<>();
                        associatedApps.put(carrierAppPackage, appList);
                    }
                    appList.add(ai);
                }
            }
        }
        return associatedApps;
    }

    private static ApplicationInfo getApplicationInfoIfSystemApp(IPackageManager packageManager, int userId, String packageName) {
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 536903680, userId);
            if (ai == null || !ai.isSystemApp()) {
                return null;
            }
            return ai;
        } catch (RemoteException e) {
            Slog.w(TAG, "Could not reach PackageManager", e);
            return null;
        }
    }
}
