package com.android.server.net;

import android.annotation.IntDef;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import com.android.server.LocalServices;
import com.android.server.am.ProcessList;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class NetworkStatsAccess {

    @IntDef({0, 1, 2, 3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Level {
        public static final int DEFAULT = 0;
        public static final int DEVICE = 3;
        public static final int DEVICESUMMARY = 2;
        public static final int USER = 1;
    }

    private NetworkStatsAccess() {
    }

    public static int checkAccessLevel(Context context, int callingUid, String callingPackage) {
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        boolean hasCarrierPrivileges = tm != null ? tm.checkCarrierPrivilegesForPackage(callingPackage) == 1 : false;
        boolean isActiveAdminWithPolicy = dpmi != null ? dpmi.isActiveAdminWithPolicy(callingUid, -2) : false;
        if (hasCarrierPrivileges || isActiveAdminWithPolicy || UserHandle.getAppId(callingUid) == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            return 3;
        }
        if (hasAppOpsPermission(context, callingUid, callingPackage) || context.checkCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY") == 0) {
            return 2;
        }
        boolean isProfileOwner;
        if (dpmi != null) {
            isProfileOwner = dpmi.isActiveAdminWithPolicy(callingUid, -1);
        } else {
            isProfileOwner = false;
        }
        return isProfileOwner ? 1 : 0;
    }

    public static boolean isAccessibleToUser(int uid, int callerUid, int accessLevel) {
        boolean z = true;
        switch (accessLevel) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                if (!(uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || uid == -4 || uid == -5 || UserHandle.getUserId(uid) == UserHandle.getUserId(callerUid))) {
                    z = false;
                }
                return z;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                if (!(uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || uid == -4 || uid == -5 || uid == -1 || UserHandle.getUserId(uid) == UserHandle.getUserId(callerUid))) {
                    z = false;
                }
                return z;
            case H.REPORT_LOSING_FOCUS /*3*/:
                return true;
            default:
                if (uid != callerUid) {
                    z = false;
                }
                return z;
        }
    }

    private static boolean hasAppOpsPermission(Context context, int callingUid, String callingPackage) {
        boolean z = true;
        if (callingPackage == null) {
            return false;
        }
        int mode = ((AppOpsManager) context.getSystemService("appops")).checkOp(43, callingUid, callingPackage);
        if (mode == 3) {
            if (context.checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
                z = false;
            }
            return z;
        }
        if (mode != 0) {
            z = false;
        }
        return z;
    }
}
