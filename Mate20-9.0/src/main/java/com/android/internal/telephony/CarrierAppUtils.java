package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
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

    @VisibleForTesting
    public static void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, TelephonyManager telephonyManager, ContentResolver contentResolver, int userId, ArraySet<String> systemCarrierAppsDisabledUntilUsed, ArrayMap<String, List<String>> systemCarrierAssociatedAppsDisabledUntilUsed) {
        Iterator<ApplicationInfo> it;
        boolean z;
        List<ApplicationInfo> associatedAppList;
        Iterator<ApplicationInfo> it2;
        ApplicationInfo ai;
        String packageName;
        List<ApplicationInfo> associatedAppList2;
        Iterator<ApplicationInfo> it3;
        IPackageManager iPackageManager = packageManager;
        TelephonyManager telephonyManager2 = telephonyManager;
        ContentResolver contentResolver2 = contentResolver;
        int i = userId;
        List<ApplicationInfo> candidates = getDefaultCarrierAppCandidatesHelper(iPackageManager, i, systemCarrierAppsDisabledUntilUsed);
        if (candidates == null || candidates.isEmpty()) {
            ArrayMap<String, List<String>> arrayMap = systemCarrierAssociatedAppsDisabledUntilUsed;
            return;
        }
        Map<String, List<ApplicationInfo>> associatedApps = getDefaultCarrierAssociatedAppsHelper(iPackageManager, i, systemCarrierAssociatedAppsDisabledUntilUsed);
        List<String> enabledCarrierPackages = new ArrayList<>();
        boolean z2 = false;
        boolean z3 = true;
        boolean hasRunOnce = Settings.Secure.getIntForUser(contentResolver2, "carrier_apps_handled", 0, i) == 1;
        try {
            Iterator<ApplicationInfo> it4 = candidates.iterator();
            while (it4.hasNext()) {
                ApplicationInfo ai2 = it4.next();
                String packageName2 = ai2.packageName;
                if ((telephonyManager2 == null || telephonyManager2.checkCarrierPrivilegesForPackageAnyPhone(packageName2) != z3) ? z2 : z3) {
                    if (!ai2.isUpdatedSystemApp()) {
                        if (ai2.enabledSetting != 0) {
                            if (ai2.enabledSetting != 4) {
                                it = it4;
                                packageName = packageName2;
                                ai = ai2;
                                z = z2;
                            }
                        }
                        Slog.i(TAG, "Update state(" + packageName2 + "): ENABLED for user " + i);
                        it = it4;
                        packageName = packageName2;
                        ai = ai2;
                        z = z2;
                        iPackageManager.setApplicationEnabledSetting(packageName2, 1, 1, i, callingPackage);
                    } else {
                        it = it4;
                        packageName = packageName2;
                        ai = ai2;
                        z = z2;
                    }
                    List<ApplicationInfo> associatedAppList3 = associatedApps.get(packageName);
                    if (associatedAppList3 != null) {
                        Iterator<ApplicationInfo> it5 = associatedAppList3.iterator();
                        while (it5.hasNext()) {
                            ApplicationInfo associatedApp = it5.next();
                            if (associatedApp.enabledSetting != 0) {
                                if (associatedApp.enabledSetting != 4) {
                                    it3 = it5;
                                    associatedAppList2 = associatedAppList3;
                                    it5 = it3;
                                    associatedAppList3 = associatedAppList2;
                                }
                            }
                            Slog.i(TAG, "Update associated state(" + associatedApp.packageName + "): ENABLED for user " + i);
                            ApplicationInfo applicationInfo = associatedApp;
                            it3 = it5;
                            associatedAppList2 = associatedAppList3;
                            iPackageManager.setApplicationEnabledSetting(associatedApp.packageName, 1, 1, i, callingPackage);
                            it5 = it3;
                            associatedAppList3 = associatedAppList2;
                        }
                    }
                    enabledCarrierPackages.add(ai.packageName);
                } else {
                    it = it4;
                    String packageName3 = packageName2;
                    z = z2;
                    ApplicationInfo ai3 = ai2;
                    if (ai3.isUpdatedSystemApp() || ai3.enabledSetting != 0) {
                    } else {
                        Slog.i(TAG, "Update state(" + packageName3 + "): DISABLED_UNTIL_USED for user " + i);
                        ApplicationInfo applicationInfo2 = ai3;
                        iPackageManager.setApplicationEnabledSetting(packageName3, 4, 0, i, callingPackage);
                    }
                    if (!hasRunOnce) {
                        List<ApplicationInfo> associatedAppList4 = associatedApps.get(packageName3);
                        if (associatedAppList4 != null) {
                            Iterator<ApplicationInfo> it6 = associatedAppList4.iterator();
                            while (it6.hasNext()) {
                                ApplicationInfo associatedApp2 = it6.next();
                                if (associatedApp2.enabledSetting == 0) {
                                    Slog.i(TAG, "Update associated state(" + associatedApp2.packageName + "): DISABLED_UNTIL_USED for user " + i);
                                    ApplicationInfo applicationInfo3 = associatedApp2;
                                    it2 = it6;
                                    associatedAppList = associatedAppList4;
                                    iPackageManager.setApplicationEnabledSetting(associatedApp2.packageName, 4, 0, i, callingPackage);
                                } else {
                                    it2 = it6;
                                    associatedAppList = associatedAppList4;
                                }
                                it6 = it2;
                                associatedAppList4 = associatedAppList;
                            }
                        }
                    }
                }
                z2 = z;
                it4 = it;
                z3 = true;
            }
            if (!hasRunOnce) {
                Settings.Secure.putIntForUser(contentResolver2, "carrier_apps_handled", 1, i);
            }
            if (!enabledCarrierPackages.isEmpty()) {
                String[] packageNames = new String[enabledCarrierPackages.size()];
                enabledCarrierPackages.toArray(packageNames);
                iPackageManager.grantDefaultPermissionsToEnabledCarrierApps(packageNames, i);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Could not reach PackageManager", e);
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
        if (systemCarrierAppsDisabledUntilUsed == null) {
            return null;
        }
        int size = systemCarrierAppsDisabledUntilUsed.size();
        if (size == 0) {
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
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 32768, userId);
            if (ai != null && ai.isSystemApp()) {
                return ai;
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Could not reach PackageManager", e);
        }
        return null;
    }
}
