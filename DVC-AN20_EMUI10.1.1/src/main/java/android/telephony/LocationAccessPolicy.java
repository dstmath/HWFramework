package android.telephony;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.location.LocationManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;

public final class LocationAccessPolicy {
    private static final boolean DBG = false;
    public static final int MAX_SDK_FOR_ANY_ENFORCEMENT = 10000;
    private static final String TAG = "LocationAccessPolicy";

    public enum LocationPermissionResult {
        ALLOWED,
        DENIED_SOFT,
        DENIED_HARD
    }

    public static class LocationPermissionQuery {
        public final String callingPackage;
        public final int callingPid;
        public final int callingUid;
        public final boolean logAsInfo;
        public final String method;
        public final int minSdkVersionForCoarse;
        public final int minSdkVersionForFine;

        private LocationPermissionQuery(String callingPackage2, int callingUid2, int callingPid2, int minSdkVersionForCoarse2, int minSdkVersionForFine2, boolean logAsInfo2, String method2) {
            this.callingPackage = callingPackage2;
            this.callingUid = callingUid2;
            this.callingPid = callingPid2;
            this.minSdkVersionForCoarse = minSdkVersionForCoarse2;
            this.minSdkVersionForFine = minSdkVersionForFine2;
            this.logAsInfo = logAsInfo2;
            this.method = method2;
        }

        public static class Builder {
            private String mCallingPackage;
            private int mCallingPid;
            private int mCallingUid;
            private boolean mLogAsInfo = false;
            private String mMethod;
            private int mMinSdkVersionForCoarse = Integer.MAX_VALUE;
            private int mMinSdkVersionForFine = Integer.MAX_VALUE;

            public Builder setCallingPackage(String callingPackage) {
                this.mCallingPackage = callingPackage;
                return this;
            }

            public Builder setCallingUid(int callingUid) {
                this.mCallingUid = callingUid;
                return this;
            }

            public Builder setCallingPid(int callingPid) {
                this.mCallingPid = callingPid;
                return this;
            }

            public Builder setMinSdkVersionForCoarse(int minSdkVersionForCoarse) {
                this.mMinSdkVersionForCoarse = minSdkVersionForCoarse;
                return this;
            }

            public Builder setMinSdkVersionForFine(int minSdkVersionForFine) {
                this.mMinSdkVersionForFine = minSdkVersionForFine;
                return this;
            }

            public Builder setMethod(String method) {
                this.mMethod = method;
                return this;
            }

            public Builder setLogAsInfo(boolean logAsInfo) {
                this.mLogAsInfo = logAsInfo;
                return this;
            }

            public LocationPermissionQuery build() {
                return new LocationPermissionQuery(this.mCallingPackage, this.mCallingUid, this.mCallingPid, this.mMinSdkVersionForCoarse, this.mMinSdkVersionForFine, this.mLogAsInfo, this.mMethod);
            }
        }
    }

    private static void logError(Context context, LocationPermissionQuery query, String errorMsg) {
        if (query.logAsInfo) {
            Log.i(TAG, errorMsg);
            return;
        }
        Log.e(TAG, errorMsg);
        try {
            if (Build.IS_DEBUGGABLE) {
                Toast.makeText(context, errorMsg, 0).show();
            }
        } catch (Throwable th) {
        }
    }

    private static LocationPermissionResult appOpsModeToPermissionResult(int appOpsMode) {
        if (appOpsMode == 0) {
            return LocationPermissionResult.ALLOWED;
        }
        if (appOpsMode != 2) {
            return LocationPermissionResult.DENIED_SOFT;
        }
        return LocationPermissionResult.DENIED_HARD;
    }

    private static LocationPermissionResult checkAppLocationPermissionHelper(Context context, LocationPermissionQuery query, String permissionToCheck) {
        String locationTypeForLog = Manifest.permission.ACCESS_FINE_LOCATION.equals(permissionToCheck) ? "fine" : "coarse";
        if (checkManifestPermission(context, query.callingPid, query.callingUid, permissionToCheck)) {
            int appOpMode = ((AppOpsManager) context.getSystemService(AppOpsManager.class)).noteOpNoThrow(AppOpsManager.permissionToOpCode(permissionToCheck), query.callingUid, query.callingPackage);
            if (appOpMode == 0) {
                return LocationPermissionResult.ALLOWED;
            }
            Log.i(TAG, query.callingPackage + " is aware of " + locationTypeForLog + " but the app-ops permission is specifically denied.");
            return appOpsModeToPermissionResult(appOpMode);
        }
        int minSdkVersion = Manifest.permission.ACCESS_FINE_LOCATION.equals(permissionToCheck) ? query.minSdkVersionForFine : query.minSdkVersionForCoarse;
        if (minSdkVersion > 10000) {
            logError(context, query, "Allowing " + query.callingPackage + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + locationTypeForLog + " because we're not enforcing API " + minSdkVersion + " yet. Please fix this app because it will break in the future. Called from " + query.method);
            return null;
        } else if (isAppAtLeastSdkVersion(context, query.callingPackage, minSdkVersion)) {
            return LocationPermissionResult.DENIED_HARD;
        } else {
            logError(context, query, "Allowing " + query.callingPackage + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + locationTypeForLog + " because it doesn't target API " + minSdkVersion + " yet. Please fix this app. Called from " + query.method);
            return null;
        }
    }

    public static LocationPermissionResult checkLocationPermission(Context context, LocationPermissionQuery query) {
        LocationPermissionResult resultForCoarse;
        LocationPermissionResult resultForFine;
        if (query.callingUid == 1001 || query.callingUid == 1000 || query.callingUid == 0) {
            return LocationPermissionResult.ALLOWED;
        }
        if (!checkSystemLocationAccess(context, query.callingUid, query.callingPid)) {
            return LocationPermissionResult.DENIED_SOFT;
        }
        if (query.minSdkVersionForFine < Integer.MAX_VALUE && (resultForFine = checkAppLocationPermissionHelper(context, query, Manifest.permission.ACCESS_FINE_LOCATION)) != null) {
            return resultForFine;
        }
        if (query.minSdkVersionForCoarse >= Integer.MAX_VALUE || (resultForCoarse = checkAppLocationPermissionHelper(context, query, Manifest.permission.ACCESS_COARSE_LOCATION)) == null) {
            return LocationPermissionResult.ALLOWED;
        }
        return resultForCoarse;
    }

    private static boolean checkManifestPermission(Context context, int pid, int uid, String permissionToCheck) {
        return context.checkPermission(permissionToCheck, pid, uid) == 0;
    }

    private static boolean checkSystemLocationAccess(Context context, int uid, int pid) {
        if (!isLocationModeEnabled(context, UserHandle.getUserId(uid))) {
            return false;
        }
        if (isCurrentProfile(context, uid) || checkInteractAcrossUsersFull(context, pid, uid)) {
            return true;
        }
        return false;
    }

    private static boolean isLocationModeEnabled(Context context, int userId) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LocationManager.class);
        if (locationManager != null) {
            return locationManager.isLocationEnabledForUser(UserHandle.of(userId));
        }
        Log.w(TAG, "Couldn't get location manager, denying location access");
        return false;
    }

    private static boolean checkInteractAcrossUsersFull(Context context, int pid, int uid) {
        return checkManifestPermission(context, pid, uid, Manifest.permission.INTERACT_ACROSS_USERS_FULL);
    }

    private static boolean isCurrentProfile(Context context, int uid) {
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            int callingUserId = UserHandle.getUserId(uid);
            if (callingUserId == currentUser) {
                return true;
            }
            for (UserInfo user : ((UserManager) context.getSystemService(UserManager.class)).getProfiles(currentUser)) {
                if (user.id == callingUserId) {
                    Binder.restoreCallingIdentity(token);
                    return true;
                }
            }
            Binder.restoreCallingIdentity(token);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static boolean isAppAtLeastSdkVersion(Context context, String pkgName, int sdkVersion) {
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion >= sdkVersion) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
        }
    }
}
