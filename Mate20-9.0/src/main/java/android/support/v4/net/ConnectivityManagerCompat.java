package android.support.v4.net;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ConnectivityManagerCompat {
    public static final int RESTRICT_BACKGROUND_STATUS_DISABLED = 1;
    public static final int RESTRICT_BACKGROUND_STATUS_ENABLED = 3;
    public static final int RESTRICT_BACKGROUND_STATUS_WHITELISTED = 2;

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RestrictBackgroundStatus {
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean isActiveNetworkMetered(@NonNull ConnectivityManager cm) {
        if (Build.VERSION.SDK_INT >= 16) {
            return cm.isActiveNetworkMetered();
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return true;
        }
        switch (info.getType()) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return true;
            case 1:
            case 7:
            case 9:
                return false;
            default:
                return true;
        }
    }

    @Nullable
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static NetworkInfo getNetworkInfoFromBroadcast(@NonNull ConnectivityManager cm, @NonNull Intent intent) {
        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (info != null) {
            return cm.getNetworkInfo(info.getType());
        }
        return null;
    }

    public static int getRestrictBackgroundStatus(@NonNull ConnectivityManager cm) {
        if (Build.VERSION.SDK_INT >= 24) {
            return cm.getRestrictBackgroundStatus();
        }
        return 3;
    }

    private ConnectivityManagerCompat() {
    }
}
