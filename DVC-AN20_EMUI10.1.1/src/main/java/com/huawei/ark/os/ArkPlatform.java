package com.huawei.ark.os;

import android.os.SystemProperties;

public class ArkPlatform {
    private static final int ARK_ENABLE = 1;
    private static final String ARK_PROP_NAME = "ro.maple.enable";
    private static final int ARK_ZYGOTE_DISABLE = 1;
    private static final String ARK_ZYGOTE_PROP_NAME = "persist.mygote.disable";

    public static ArkRuntimeStatus getArkRuntimeStatus() {
        if (SystemProperties.getInt(ARK_PROP_NAME, 0) != 1 || SystemProperties.getInt(ARK_ZYGOTE_PROP_NAME, 0) == 1) {
            return ArkRuntimeStatus.ABNORMAL;
        }
        return ArkRuntimeStatus.NORMAL;
    }
}
