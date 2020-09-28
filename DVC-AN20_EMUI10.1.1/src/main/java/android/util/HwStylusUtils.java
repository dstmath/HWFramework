package android.util;

import android.content.Context;
import android.content.pm.PackageManager;

public final class HwStylusUtils {
    public static final String STYLUS_FEATURE_NAME = "huawei.android.hardware.stylus";
    public static Boolean mHasStylusFeature;

    public static boolean hasStylusFeature(Context context) {
        PackageManager pm;
        Boolean bool = mHasStylusFeature;
        if (bool != null) {
            return bool.booleanValue();
        }
        if (context == null || (pm = context.getPackageManager()) == null) {
            return false;
        }
        mHasStylusFeature = Boolean.valueOf(pm.hasSystemFeature(STYLUS_FEATURE_NAME));
        return mHasStylusFeature.booleanValue();
    }
}
