package com.android.server;

import android.os.SystemProperties;

public class VSimTelephonyRegistry {
    static final boolean isVSimSupport = SystemProperties.getBoolean("ro.radio.vsim_support", false);

    public static int processVSimPhoneNumbers(int num) {
        if (!isVSimSupport || num != 2) {
            return num;
        }
        return num + 1;
    }

    static int getDefaultPhoneIdForVSim(int defaultPhoneId, int subId) {
        if (subId == 999999) {
            return 2;
        }
        return defaultPhoneId;
    }
}
