package android.telephony;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

public final class LocationAccessPolicy {
    private static final String LOG_TAG = LocationAccessPolicy.class.getSimpleName();

    public static boolean canAccessCellLocation(Context context, String pkgName, int uid, int pid, boolean throwOnDeniedPermission) throws SecurityException {
        Trace.beginSection("TelephonyLohcationCheck");
        boolean z = true;
        if (uid == 1001) {
            Trace.endSection();
            return true;
        }
        if (throwOnDeniedPermission) {
            try {
                context.enforcePermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid, "canAccessCellLocation");
            } catch (Throwable th) {
                Trace.endSection();
                throw th;
            }
        } else if (context.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == -1) {
            Trace.endSection();
            return false;
        }
        int opCode = AppOpsManager.permissionToOpCode("android.permission.ACCESS_COARSE_LOCATION");
        if (opCode != -1 && ((AppOpsManager) context.getSystemService(AppOpsManager.class)).noteOpNoThrow(opCode, uid, pkgName) != 0) {
            Trace.endSection();
            return false;
        } else if (!isLocationModeEnabled(context, UserHandle.getUserId(uid))) {
            Trace.endSection();
            return false;
        } else {
            if (!isCurrentProfile(context, uid) && !checkInteractAcrossUsersFull(context)) {
                z = false;
            }
            Trace.endSection();
            return z;
        }
    }

    private static boolean isLocationModeEnabled(Context context, int userId) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LocationManager.class);
        if (locationManager != null) {
            return locationManager.isLocationEnabledForUser(UserHandle.of(userId));
        }
        Log.w(LOG_TAG, "Couldn't get location manager, denying location access");
        return false;
    }

    private static boolean checkInteractAcrossUsersFull(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0;
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
}
