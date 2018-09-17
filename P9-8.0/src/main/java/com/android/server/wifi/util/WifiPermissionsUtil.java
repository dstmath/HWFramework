package com.android.server.wifi.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.os.RemoteException;
import android.os.UserManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiLog;
import com.android.server.wifi.WifiSettingsStore;

public class WifiPermissionsUtil {
    private static final String TAG = "WifiPermissionsUtil";
    private final AppOpsManager mAppOps = ((AppOpsManager) this.mContext.getSystemService("appops"));
    private final Context mContext;
    private WifiLog mLog;
    private final NetworkScoreManager mNetworkScoreManager;
    private final WifiSettingsStore mSettingsStore;
    private final UserManager mUserManager;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;

    public WifiPermissionsUtil(WifiPermissionsWrapper wifiPermissionsWrapper, Context context, WifiSettingsStore settingsStore, UserManager userManager, NetworkScoreManager networkScoreManager, WifiInjector wifiInjector) {
        this.mWifiPermissionsWrapper = wifiPermissionsWrapper;
        this.mContext = context;
        this.mUserManager = userManager;
        this.mSettingsStore = settingsStore;
        this.mLog = wifiInjector.makeLog(TAG);
        this.mNetworkScoreManager = networkScoreManager;
    }

    public boolean checkConfigOverridePermission(int uid) {
        boolean z = false;
        try {
            if (this.mWifiPermissionsWrapper.getOverrideWifiConfigPermission(uid) == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            this.mLog.err("Error checking for permission: %").r(e.getMessage()).flush();
            return false;
        }
    }

    public void enforceTetherChangePermission(Context context) {
        ConnectivityManager.enforceTetherChangePermission(context);
    }

    public void enforceLocationPermission(String pkgName, int uid) {
        if (!checkCallersLocationPermission(pkgName, uid)) {
            throw new SecurityException("UID " + uid + " does not have Location permission");
        }
    }

    public boolean canAccessScanResults(String pkgName, int uid, int minVersion) throws SecurityException {
        boolean canCallingUidAccessLocation;
        this.mAppOps.checkPackage(uid, pkgName);
        if (checkCallerHasPeersMacAddressPermission(uid)) {
            canCallingUidAccessLocation = true;
        } else {
            canCallingUidAccessLocation = isCallerActiveNwScorer(uid);
        }
        int canAppPackageUseLocation;
        if (minVersion == 23) {
            if (!isLocationModeEnabled(pkgName, this.mWifiPermissionsWrapper.getCallingUserId(uid))) {
                canAppPackageUseLocation = 0;
            } else if (checkCallersLocationPermission(pkgName, uid)) {
                canAppPackageUseLocation = 1;
            } else {
                canAppPackageUseLocation = isLegacyForeground(pkgName, minVersion);
            }
        } else if (isLegacyForeground(pkgName, minVersion)) {
            canAppPackageUseLocation = 1;
        } else if (isLocationModeEnabled(pkgName, this.mWifiPermissionsWrapper.getCallingUserId(uid))) {
            canAppPackageUseLocation = checkCallersLocationPermission(pkgName, uid);
        } else {
            canAppPackageUseLocation = 0;
        }
        if (!canCallingUidAccessLocation && (canAppPackageUseLocation ^ 1) != 0) {
            this.mLog.tC("Denied: no location permission");
            return false;
        } else if (!isScanAllowedbyApps(pkgName, uid)) {
            this.mLog.tC("Denied: app wifi scan not allowed");
            return false;
        } else if (isCurrentProfile(uid) || (checkInteractAcrossUsersFull(uid) ^ 1) == 0) {
            return true;
        } else {
            this.mLog.tC("Denied: Profile not permitted");
            return false;
        }
    }

    private boolean checkCallerHasPeersMacAddressPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.PEERS_MAC_ADDRESS", uid) == 0;
    }

    private boolean isCallerActiveNwScorer(int uid) {
        return this.mNetworkScoreManager.isCallerActiveScorer(uid);
    }

    private boolean isScanAllowedbyApps(String pkgName, int uid) {
        return checkAppOpAllowed(10, pkgName, uid);
    }

    private boolean checkInteractAcrossUsersFull(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.INTERACT_ACROSS_USERS_FULL", uid) == 0;
    }

    private boolean isCurrentProfile(int uid) {
        int currentUser = this.mWifiPermissionsWrapper.getCurrentUser();
        int callingUserId = this.mWifiPermissionsWrapper.getCallingUserId(uid);
        if (callingUserId == currentUser) {
            return true;
        }
        for (UserInfo user : this.mUserManager.getProfiles(currentUser)) {
            if (user.id == callingUserId) {
                return true;
            }
        }
        return false;
    }

    private boolean isLegacyVersion(String pkgName, int minVersion) {
        try {
            if (this.mContext.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion < minVersion) {
                return true;
            }
        } catch (NameNotFoundException e) {
        }
        return false;
    }

    private boolean checkAppOpAllowed(int op, String pkgName, int uid) {
        return this.mAppOps.noteOp(op, uid, pkgName) == 0;
    }

    private boolean isLegacyForeground(String pkgName, int version) {
        return isLegacyVersion(pkgName, version) ? isForegroundApp(pkgName) : false;
    }

    private boolean isForegroundApp(String pkgName) {
        return pkgName.equals(this.mWifiPermissionsWrapper.getTopPkgName());
    }

    private boolean checkCallersLocationPermission(String pkgName, int uid) {
        if (this.mWifiPermissionsWrapper.getUidPermission("android.permission.ACCESS_COARSE_LOCATION", uid) == 0 && checkAppOpAllowed(0, pkgName, uid)) {
            return true;
        }
        return false;
    }

    private boolean isLocationModeEnabled(String pkgName, int userId) {
        return this.mSettingsStore.getLocationModeSetting(this.mContext, userId) != 0;
    }
}
