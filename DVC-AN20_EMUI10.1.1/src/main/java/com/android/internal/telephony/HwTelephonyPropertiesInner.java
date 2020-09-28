package com.android.internal.telephony;

import com.huawei.android.os.SystemPropertiesEx;

public class HwTelephonyPropertiesInner {
    public static final boolean ENABLE_NEW_PDP_SCHEME = SystemPropertiesEx.getBoolean("ro.config.newpdpscheme.enabled", true);
    public static final String MULTI_PDP_PLMN_MATCHED = "gsm.multipdp.plmn.matched";
    public static final String PROP_SINGLE_PDP_HPLMN_MATCHED = "gsm.singlepdp.hplmn.matched";

    private HwTelephonyPropertiesInner() {
    }
}
