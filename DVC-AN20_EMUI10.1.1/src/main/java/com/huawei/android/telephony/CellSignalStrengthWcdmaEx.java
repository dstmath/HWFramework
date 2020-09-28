package com.huawei.android.telephony;

import android.telephony.CellSignalStrengthWcdma;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CellSignalStrengthWcdmaEx {
    public static int getRscp(CellSignalStrengthWcdma signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getRscp();
        }
        return SignalStrengthEx.INVALID;
    }

    public static int getEcio(CellSignalStrengthWcdma signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getEcio();
        }
        return SignalStrengthEx.INVALID;
    }

    public static void setRssi(CellSignalStrengthWcdma signalStrength, int rssi) {
        if (signalStrength != null) {
            signalStrength.setRssi(rssi);
        }
    }

    public static void setRscp(CellSignalStrengthWcdma signalStrength, int rscp) {
        if (signalStrength != null) {
            signalStrength.setRscp(rscp);
        }
    }

    public static void setEcio(CellSignalStrengthWcdma signalStrength, int ecio) {
        if (signalStrength != null) {
            signalStrength.setEcio(ecio);
        }
    }

    public static void setBitErrorRate(CellSignalStrengthWcdma signalStrength, int bitErrorRate) {
        if (signalStrength != null) {
            signalStrength.setBitErrorRate(bitErrorRate);
        }
    }

    public static void setEcNo(CellSignalStrengthWcdma signalStrength, int ecno) {
        if (signalStrength != null) {
            signalStrength.setEcNo(ecno);
        }
    }

    public static int getPhoneId(CellSignalStrengthWcdma signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getPhoneId();
        }
        return -1;
    }
}
