package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.Resources;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.R;
import com.android.server.SystemConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CarrierAppUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "CarrierAppUtils";

    private CarrierAppUtils() {
    }

    public static synchronized void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, TelephonyManager telephonyManager, ContentResolver contentResolver, int userId) {
        synchronized (CarrierAppUtils.class) {
            String str = callingPackage;
            IPackageManager iPackageManager = packageManager;
            TelephonyManager telephonyManager2 = telephonyManager;
            ContentResolver contentResolver2 = contentResolver;
            int i = userId;
            disableCarrierAppsUntilPrivileged(str, iPackageManager, telephonyManager2, contentResolver2, i, Resources.getSystem().getStringArray(R.array.config_disabledUntilUsedPreinstalledCarrierApps), SystemConfig.getInstance().getDisabledUntilUsedPreinstalledCarrierAssociatedApps());
        }
    }

    public static synchronized void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, ContentResolver contentResolver, int userId) {
        synchronized (CarrierAppUtils.class) {
            String str = callingPackage;
            IPackageManager iPackageManager = packageManager;
            ContentResolver contentResolver2 = contentResolver;
            int i = userId;
            disableCarrierAppsUntilPrivileged(str, iPackageManager, null, contentResolver2, i, Resources.getSystem().getStringArray(R.array.config_disabledUntilUsedPreinstalledCarrierApps), SystemConfig.getInstance().getDisabledUntilUsedPreinstalledCarrierAssociatedApps());
        }
    }

    public static void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, TelephonyManager telephonyManager, ContentResolver contentResolver, int userId, String[] systemCarrierAppsDisabledUntilUsed, ArrayMap<String, List<String>> systemCarrierAssociatedAppsDisabledUntilUsed) {
        List<ApplicationInfo> candidates = getDefaultCarrierAppCandidatesHelper(packageManager, userId, systemCarrierAppsDisabledUntilUsed);
        if (candidates != null && !candidates.isEmpty()) {
            Map<String, List<ApplicationInfo>> associatedApps = getDefaultCarrierAssociatedAppsHelper(packageManager, userId, systemCarrierAssociatedAppsDisabledUntilUsed);
            List<String> enabledCarrierPackages = new ArrayList();
            boolean hasRunOnce = Secure.getIntForUser(contentResolver, Secure.CARRIER_APPS_HANDLED, 0, userId) == 1;
            try {
                for (ApplicationInfo ai : candidates) {
                    String packageName = ai.packageName;
                    boolean hasPrivileges = telephonyManager != null ? telephonyManager.checkCarrierPrivilegesForPackageAnyPhone(packageName) == 1 : false;
                    List<ApplicationInfo> associatedAppList;
                    if (hasPrivileges) {
                        if (!ai.isUpdatedSystemApp() && (ai.enabledSetting == 0 || ai.enabledSetting == 4)) {
                            Slog.i(TAG, "Update state(" + packageName + "): ENABLED for user " + userId);
                            packageManager.setApplicationEnabledSetting(packageName, 1, 1, userId, callingPackage);
                        }
                        associatedAppList = (List) associatedApps.get(packageName);
                        if (associatedAppList != null) {
                            for (ApplicationInfo associatedApp : associatedAppList) {
                                if (associatedApp.enabledSetting == 0 || associatedApp.enabledSetting == 4) {
                                    Slog.i(TAG, "Update associated state(" + associatedApp.packageName + "): ENABLED for user " + userId);
                                    packageManager.setApplicationEnabledSetting(associatedApp.packageName, 1, 1, userId, callingPackage);
                                }
                            }
                        }
                        enabledCarrierPackages.add(ai.packageName);
                    } else {
                        if (!ai.isUpdatedSystemApp() && ai.enabledSetting == 0) {
                            Slog.i(TAG, "Update state(" + packageName + "): DISABLED_UNTIL_USED for user " + userId);
                            packageManager.setApplicationEnabledSetting(packageName, 4, 0, userId, callingPackage);
                        }
                        if (!hasRunOnce) {
                            associatedAppList = (List) associatedApps.get(packageName);
                            if (associatedAppList != null) {
                                for (ApplicationInfo associatedApp2 : associatedAppList) {
                                    if (associatedApp2.enabledSetting == 0) {
                                        Slog.i(TAG, "Update associated state(" + associatedApp2.packageName + "): DISABLED_UNTIL_USED for user " + userId);
                                        packageManager.setApplicationEnabledSetting(associatedApp2.packageName, 4, 0, userId, callingPackage);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!hasRunOnce) {
                    Secure.putIntForUser(contentResolver, Secure.CARRIER_APPS_HANDLED, 1, userId);
                }
                if (!enabledCarrierPackages.isEmpty()) {
                    String[] packageNames = new String[enabledCarrierPackages.size()];
                    enabledCarrierPackages.toArray(packageNames);
                    packageManager.grantDefaultPermissionsToEnabledCarrierApps(packageNames, userId);
                }
            } catch (Throwable e) {
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
            if (!(telephonyManager.checkCarrierPrivilegesForPackageAnyPhone(((ApplicationInfo) candidates.get(i)).packageName) == 1)) {
                candidates.remove(i);
            }
        }
        return candidates;
    }

    public static List<ApplicationInfo> getDefaultCarrierAppCandidates(IPackageManager packageManager, int userId) {
        return getDefaultCarrierAppCandidatesHelper(packageManager, userId, Resources.getSystem().getStringArray(R.array.config_disabledUntilUsedPreinstalledCarrierApps));
    }

    private static List<ApplicationInfo> getDefaultCarrierAppCandidatesHelper(IPackageManager packageManager, int userId, String[] systemCarrierAppsDisabledUntilUsed) {
        if (systemCarrierAppsDisabledUntilUsed == null || systemCarrierAppsDisabledUntilUsed.length == 0) {
            return null;
        }
        List<ApplicationInfo> apps = new ArrayList(systemCarrierAppsDisabledUntilUsed.length);
        for (String packageName : systemCarrierAppsDisabledUntilUsed) {
            ApplicationInfo ai = getApplicationInfoIfSystemApp(packageManager, userId, packageName);
            if (ai != null) {
                apps.add(ai);
            }
        }
        return apps;
    }

    private static Map<String, List<ApplicationInfo>> getDefaultCarrierAssociatedAppsHelper(IPackageManager packageManager, int userId, ArrayMap<String, List<String>> systemCarrierAssociatedAppsDisabledUntilUsed) {
        int size = systemCarrierAssociatedAppsDisabledUntilUsed.size();
        Map<String, List<ApplicationInfo>> associatedApps = new ArrayMap(size);
        for (int i = 0; i < size; i++) {
            String carrierAppPackage = (String) systemCarrierAssociatedAppsDisabledUntilUsed.keyAt(i);
            List<String> associatedAppPackages = (List) systemCarrierAssociatedAppsDisabledUntilUsed.valueAt(i);
            for (int j = 0; j < associatedAppPackages.size(); j++) {
                ApplicationInfo ai = getApplicationInfoIfSystemApp(packageManager, userId, (String) associatedAppPackages.get(j));
                if (!(ai == null || (ai.isUpdatedSystemApp() ^ 1) == 0)) {
                    List<ApplicationInfo> appList = (List) associatedApps.get(carrierAppPackage);
                    if (appList == null) {
                        appList = new ArrayList();
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
            if (ai == null || !ai.isSystemApp()) {
                return null;
            }
            return ai;
        } catch (RemoteException e) {
            Slog.w(TAG, "Could not reach PackageManager", e);
        }
    }
}
