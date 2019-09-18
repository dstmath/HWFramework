package com.android.internal.telephony.util;

import android.os.SystemProperties;

public class HwCustUtil {
    public static final boolean isVZW = ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")));
}
