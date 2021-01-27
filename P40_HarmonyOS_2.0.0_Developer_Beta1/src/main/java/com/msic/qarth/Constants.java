package com.msic.qarth;

import android.os.SystemProperties;

public class Constants {
    public static String COMMON_PATCH_PKG_NAME = "COMMON_HOOK";
    public static boolean DEBUG = false;
    public static String FWK_HOT_PATCH_PATH = "/patch_hw/fwkhotpatch/";
    private static final String TAG = "Constants";

    static {
        boolean z = false;
        if (SystemProperties.getInt("qarth.debug", 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }
}
