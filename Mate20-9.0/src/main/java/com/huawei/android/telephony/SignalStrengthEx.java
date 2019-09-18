package com.huawei.android.telephony;

import android.telephony.SignalStrength;

public class SignalStrengthEx {
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;

    public static int getLteRsrp(SignalStrength signal) {
        if (signal != null) {
            return signal.getLteRsrp();
        }
        return Integer.MAX_VALUE;
    }

    public static int getLteRssnr(SignalStrength signal) {
        if (signal != null) {
            return signal.getLteRssnr();
        }
        return Integer.MAX_VALUE;
    }

    public static int getWcdmaRscp(SignalStrength signal) {
        if (signal != null) {
            return signal.getWcdmaRscp();
        }
        return Integer.MAX_VALUE;
    }

    public static int getWcdmaEcio(SignalStrength signal) {
        if (signal != null) {
            return signal.getWcdmaEcio();
        }
        return Integer.MAX_VALUE;
    }

    public static int getEvdoLevel(SignalStrength signal) {
        if (signal != null) {
            return signal.getEvdoLevel();
        }
        return 0;
    }

    public static int getCdmaLevel(SignalStrength signal) {
        if (signal != null) {
            return signal.getCdmaLevel();
        }
        return 0;
    }
}
