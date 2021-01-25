package android.content;

import android.app.AppOpsManager;
import android.os.Binder;
import android.os.Process;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PermissionChecker {
    public static final int PERMISSION_DENIED = -1;
    public static final int PERMISSION_DENIED_APP_OP = -2;
    public static final int PERMISSION_GRANTED = 0;
    public static final int PID_UNKNOWN = -1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionResult {
    }

    private PermissionChecker() {
    }

    public static int checkPermissionForDataDelivery(Context context, String permission, int pid, int uid, String packageName) {
        return checkPermissionCommon(context, permission, pid, uid, packageName, true);
    }

    public static int checkPermissionForPreflight(Context context, String permission, int pid, int uid, String packageName) {
        return checkPermissionCommon(context, permission, pid, uid, packageName, false);
    }

    public static int checkSelfPermissionForDataDelivery(Context context, String permission) {
        return checkPermissionForDataDelivery(context, permission, Process.myPid(), Process.myUid(), context.getPackageName());
    }

    public static int checkSelfPermissionForPreflight(Context context, String permission) {
        return checkPermissionForPreflight(context, permission, Process.myPid(), Process.myUid(), context.getPackageName());
    }

    public static int checkCallingPermissionForDataDelivery(Context context, String permission, String packageName) {
        if (Binder.getCallingPid() == Process.myPid()) {
            return -1;
        }
        return checkPermissionForDataDelivery(context, permission, Binder.getCallingPid(), Binder.getCallingUid(), packageName);
    }

    public static int checkCallingPermissionForPreflight(Context context, String permission, String packageName) {
        if (Binder.getCallingPid() == Process.myPid()) {
            return -1;
        }
        return checkPermissionForPreflight(context, permission, Binder.getCallingPid(), Binder.getCallingUid(), packageName);
    }

    public static int checkCallingOrSelfPermissionForDataDelivery(Context context, String permission) {
        return checkPermissionForDataDelivery(context, permission, Binder.getCallingPid(), Binder.getCallingUid(), Binder.getCallingPid() == Process.myPid() ? context.getPackageName() : null);
    }

    public static int checkCallingOrSelfPermissionForPreflight(Context context, String permission) {
        return checkPermissionForPreflight(context, permission, Binder.getCallingPid(), Binder.getCallingUid(), Binder.getCallingPid() == Process.myPid() ? context.getPackageName() : null);
    }

    private static int checkPermissionCommon(Context context, String permission, int pid, int uid, String packageName, boolean forDataDelivery) {
        if (context.checkPermission(permission, pid, uid) == -1) {
            return -1;
        }
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        String op = AppOpsManager.permissionToOp(permission);
        if (op == null) {
            return 0;
        }
        if (packageName == null) {
            String[] packageNames = context.getPackageManager().getPackagesForUid(uid);
            if (packageNames == null || packageNames.length <= 0) {
                return -1;
            }
            packageName = packageNames[0];
        }
        if (!forDataDelivery) {
            int mode = appOpsManager.unsafeCheckOpRawNoThrow(op, uid, packageName);
            if (mode != 0 && mode != 4) {
                return -2;
            }
        } else if (appOpsManager.noteProxyOpNoThrow(op, packageName, uid) != 0) {
            return -2;
        }
        return 0;
    }
}
