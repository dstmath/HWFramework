package com.android.internal.telephony;

import com.huawei.android.os.SystemPropertiesEx;

public class HwCustUtil {
    public static final boolean isVZW = ("389".equals(SystemPropertiesEx.get("ro.config.hw_opta")) && "840".equals(SystemPropertiesEx.get("ro.config.hw_optb")));
}
