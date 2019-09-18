package android.support.v4.content;

import android.content.Context;
import android.os.Binder;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.app.AppOpsManagerCompat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PermissionChecker {
    public static final int PERMISSION_DENIED = -1;
    public static final int PERMISSION_DENIED_APP_OP = -2;
    public static final int PERMISSION_GRANTED = 0;

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionResult {
    }

    private PermissionChecker() {
    }

    public static int checkPermission(@NonNull Context context, @NonNull String permission, int pid, int uid, @Nullable String packageName) {
        if (context.checkPermission(permission, pid, uid) == -1) {
            return -1;
        }
        String op = AppOpsManagerCompat.permissionToOp(permission);
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
        if (AppOpsManagerCompat.noteProxyOpNoThrow(context, op, packageName) != 0) {
            return -2;
        }
        return 0;
    }

    public static int checkSelfPermission(@NonNull Context context, @NonNull String permission) {
        return checkPermission(context, permission, Process.myPid(), Process.myUid(), context.getPackageName());
    }

    public static int checkCallingPermission(@NonNull Context context, @NonNull String permission, @Nullable String packageName) {
        if (Binder.getCallingPid() == Process.myPid()) {
            return -1;
        }
        return checkPermission(context, permission, Binder.getCallingPid(), Binder.getCallingUid(), packageName);
    }

    public static int checkCallingOrSelfPermission(@NonNull Context context, @NonNull String permission) {
        return checkPermission(context, permission, Binder.getCallingPid(), Binder.getCallingUid(), Binder.getCallingPid() == Process.myPid() ? context.getPackageName() : null);
    }
}
