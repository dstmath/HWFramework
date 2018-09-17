package android.util;

import android.content.Context;
import android.content.pm.PackageManager;

public final class HwStylusUtils {
    public static final String STYLUS_FEATURE_NAME = "huawei.android.hardware.stylus";

    public static boolean hasStylusFeature(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                return pm.hasSystemFeature(STYLUS_FEATURE_NAME);
            }
        }
        return false;
    }
}
