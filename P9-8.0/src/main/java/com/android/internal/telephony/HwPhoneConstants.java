package com.android.internal.telephony;

import android.os.SystemProperties;

public class HwPhoneConstants {
    public static final int CHANGE_EC_SWITCH = 8;
    public static final String FEATURE_ENABLE_MMS_SUB1 = "enableMMS_sub1";
    public static final String FEATURE_ENABLE_MMS_SUB2 = "enableMMS_sub2";
    public static final int GET_EC_TEST = 3;
    public static final int GET_KMC_KEY = 5;
    public static final int GET_RAND_NUM = 6;
    public static final boolean IS_CHINA_TELECOM;
    public static final int IS_SHOW_MENU = 7;
    public static final int SET_EC_TEST = 2;
    public static final int SET_KMC_KEY = 4;

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            equals = false;
        }
        IS_CHINA_TELECOM = equals;
    }
}
