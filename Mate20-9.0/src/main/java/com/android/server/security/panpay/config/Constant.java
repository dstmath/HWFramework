package com.android.server.security.panpay.config;

import android.os.SystemProperties;

public class Constant {
    public static final boolean IS_UKEY_SWITCH_ON = SystemProperties.getBoolean("ro.config.hw_ukey_on", false);
    public static final boolean NFCDK_SWITCH_ON = SystemProperties.getBoolean("ro.config.hw_DK_CCC1.0_on", false);
    public static final int UKEY_VERSION = SystemProperties.getInt("ro.config.hw_ukey_version", 1);
}
