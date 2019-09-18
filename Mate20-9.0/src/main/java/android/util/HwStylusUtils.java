package android.util;

import android.content.Context;
import android.content.pm.PackageManager;

public final class HwStylusUtils {
    public static final String STYLUS_FEATURE_NAME = "huawei.android.hardware.stylus";
    private static Boolean mHasStylusFeature;

    public static boolean hasStylusFeature(Context context) {
        if (mHasStylusFeature != null) {
            return mHasStylusFeature.booleanValue();
        }
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                mHasStylusFeature = Boolean.valueOf(pm.hasSystemFeature(STYLUS_FEATURE_NAME));
                return mHasStylusFeature.booleanValue();
            }
        }
        return false;
    }
}
