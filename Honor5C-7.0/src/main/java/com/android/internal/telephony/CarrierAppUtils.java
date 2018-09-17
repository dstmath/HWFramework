package com.android.internal.telephony;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.Resources;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;

public final class CarrierAppUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "CarrierAppUtils";

    private CarrierAppUtils() {
    }

    public static synchronized void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, TelephonyManager telephonyManager, int userId) {
        synchronized (CarrierAppUtils.class) {
            disableCarrierAppsUntilPrivileged(callingPackage, packageManager, telephonyManager, userId, Resources.getSystem().getStringArray(R.array.config_disabledUntilUsedPreinstalledCarrierApps));
        }
    }

    public static synchronized void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, int userId) {
        synchronized (CarrierAppUtils.class) {
            disableCarrierAppsUntilPrivileged(callingPackage, packageManager, null, userId, Resources.getSystem().getStringArray(R.array.config_disabledUntilUsedPreinstalledCarrierApps));
        }
    }

    public static void disableCarrierAppsUntilPrivileged(String callingPackage, IPackageManager packageManager, TelephonyManager telephonyManager, int userId, String[] systemCarrierAppsDisabledUntilUsed) {
        List<ApplicationInfo> candidates = getDefaultCarrierAppCandidatesHelper(packageManager, userId, systemCarrierAppsDisabledUntilUsed);
        if (candidates != null && !candidates.isEmpty()) {
            List<String> enabledCarrierPackages = new ArrayList();
            try {
                for (ApplicationInfo ai : candidates) {
                    String packageName = ai.packageName;
                    boolean hasPrivileges = telephonyManager != null ? telephonyManager.checkCarrierPrivilegesForPackageAnyPhone(packageName) == 1 ? true : DEBUG : DEBUG;
                    if (!ai.isUpdatedSystemApp()) {
                        if (hasPrivileges && (ai.enabledSetting == 0 || ai.enabledSetting == 4)) {
                            Slog.i(TAG, "Update state(" + packageName + "): ENABLED for user " + userId);
                            packageManager.setApplicationEnabledSetting(packageName, 1, 1, userId, callingPackage);
                        } else if (!hasPrivileges) {
                            if (ai.enabledSetting == 0) {
                                Slog.i(TAG, "Update state(" + packageName + "): DISABLED_UNTIL_USED for user " + userId);
                                packageManager.setApplicationEnabledSetting(packageName, 4, 0, userId, callingPackage);
                            }
                        }
                    }
                    if (hasPrivileges) {
                        enabledCarrierPackages.add(ai.packageName);
                    }
                }
                if (!enabledCarrierPackages.isEmpty()) {
                    String[] packageNames = new String[enabledCarrierPackages.size()];
                    enabledCarrierPackages.toArray(packageNames);
                    packageManager.grantDefaultPermissionsToEnabledCarrierApps(packageNames, userId);
                }
            } catch (RemoteException e) {
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
            boolean hasPrivileges;
            if (telephonyManager.checkCarrierPrivilegesForPackageAnyPhone(((ApplicationInfo) candidates.get(i)).packageName) == 1) {
                hasPrivileges = true;
            } else {
                hasPrivileges = DEBUG;
            }
            if (!hasPrivileges) {
                candidates.remove(i);
            }
        }
        return candidates;
    }

    public static List<ApplicationInfo> getDefaultCarrierAppCandidates(IPackageManager packageManager, int userId) {
        return getDefaultCarrierAppCandidatesHelper(packageManager, userId, Resources.getSystem().getStringArray(R.array.config_disabledUntilUsedPreinstalledCarrierApps));
    }

    private static List<ApplicationInfo> getDefaultCarrierAppCandidatesHelper(IPackageManager packageManager, int userId, String[] systemCarrierAppsDisabledUntilUsed) {
        RemoteException e;
        if (systemCarrierAppsDisabledUntilUsed == null || systemCarrierAppsDisabledUntilUsed.length == 0) {
            return null;
        }
        List<ApplicationInfo> list = null;
        try {
            List<ApplicationInfo> apps = new ArrayList(systemCarrierAppsDisabledUntilUsed.length);
            try {
                for (String packageName : systemCarrierAppsDisabledUntilUsed) {
                    ApplicationInfo ai = packageManager.getApplicationInfo(packageName, AccessibilityNodeInfo.ACTION_PASTE, userId);
                    if (ai != null && ai.isSystemApp()) {
                        apps.add(ai);
                    }
                }
                list = apps;
            } catch (RemoteException e2) {
                e = e2;
                list = apps;
                Slog.w(TAG, "Could not reach PackageManager", e);
                return list;
            }
        } catch (RemoteException e3) {
            e = e3;
            Slog.w(TAG, "Could not reach PackageManager", e);
            return list;
        }
        return list;
    }
}
