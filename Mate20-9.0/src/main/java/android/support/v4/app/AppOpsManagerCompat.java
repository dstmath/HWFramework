package android.support.v4.app;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class AppOpsManagerCompat {
    public static final int MODE_ALLOWED = 0;
    public static final int MODE_DEFAULT = 3;
    public static final int MODE_ERRORED = 2;
    public static final int MODE_IGNORED = 1;

    private AppOpsManagerCompat() {
    }

    @Nullable
    public static String permissionToOp(@NonNull String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            return AppOpsManager.permissionToOp(permission);
        }
        return null;
    }

    public static int noteOp(@NonNull Context context, @NonNull String op, int uid, @NonNull String packageName) {
        if (Build.VERSION.SDK_INT >= 19) {
            return ((AppOpsManager) context.getSystemService("appops")).noteOp(op, uid, packageName);
        }
        return 1;
    }

    public static int noteOpNoThrow(@NonNull Context context, @NonNull String op, int uid, @NonNull String packageName) {
        if (Build.VERSION.SDK_INT >= 19) {
            return ((AppOpsManager) context.getSystemService("appops")).noteOpNoThrow(op, uid, packageName);
        }
        return 1;
    }

    public static int noteProxyOp(@NonNull Context context, @NonNull String op, @NonNull String proxiedPackageName) {
        if (Build.VERSION.SDK_INT >= 23) {
            return ((AppOpsManager) context.getSystemService(AppOpsManager.class)).noteProxyOp(op, proxiedPackageName);
        }
        return 1;
    }

    public static int noteProxyOpNoThrow(@NonNull Context context, @NonNull String op, @NonNull String proxiedPackageName) {
        if (Build.VERSION.SDK_INT >= 23) {
            return ((AppOpsManager) context.getSystemService(AppOpsManager.class)).noteProxyOpNoThrow(op, proxiedPackageName);
        }
        return 1;
    }
}
