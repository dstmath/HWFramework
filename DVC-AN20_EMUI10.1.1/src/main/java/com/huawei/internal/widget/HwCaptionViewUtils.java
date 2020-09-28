package com.huawei.internal.widget;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public final class HwCaptionViewUtils {
    private static final String TAG = "HwCaptionViewUtils";

    private HwCaptionViewUtils() {
    }

    public static boolean isInSubFoldDisplayMode(Context context) {
        if (context == null) {
            Log.w(TAG, "isInSubFoldDisplayMode check failed!");
            return false;
        } else if (Settings.Global.getInt(context.getContentResolver(), ConstantValues.HW_FOLD_DISPLAY_MODE_STR_PREPARE, 0) == 3) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInLazyMode(Context context) {
        if (context == null) {
            Log.w(TAG, "isInLazyMode check failed!");
            return false;
        }
        String lazyModeStr = Settings.Global.getString(context.getContentResolver(), "single_hand_mode");
        if (ConstantValues.LEFT_HAND_LAZY_MODE_STR.equals(lazyModeStr) || ConstantValues.RIGHT_HAND_LAZY_MODE_STR.equals(lazyModeStr)) {
            return true;
        }
        return false;
    }
}
