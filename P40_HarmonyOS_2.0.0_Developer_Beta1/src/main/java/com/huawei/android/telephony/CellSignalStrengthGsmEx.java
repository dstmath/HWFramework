package com.huawei.android.telephony;

import android.telephony.CellSignalStrengthGsm;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CellSignalStrengthGsmEx {
    public static int getRssi(CellSignalStrengthGsm signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getRssi();
        }
        return SignalStrengthEx.INVALID;
    }

    public static void setRssi(CellSignalStrengthGsm signalStrength, int rssi) {
        if (signalStrength != null) {
            signalStrength.setRssi(rssi);
        }
    }

    public static void setBitErrorRate(CellSignalStrengthGsm signalStrength, int bitErrorRate) {
        if (signalStrength != null) {
            signalStrength.setBitErrorRate(bitErrorRate);
        }
    }

    public static void setTimingAdvance(CellSignalStrengthGsm signalStrength, int timingAdvance) {
        if (signalStrength != null) {
            signalStrength.setTimingAdvance(timingAdvance);
        }
    }

    public static int getPhoneId(CellSignalStrengthGsm signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getPhoneId();
        }
        return -1;
    }
}
