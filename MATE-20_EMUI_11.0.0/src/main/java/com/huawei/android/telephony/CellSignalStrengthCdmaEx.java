package com.huawei.android.telephony;

import android.telephony.CellSignalStrengthCdma;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CellSignalStrengthCdmaEx {
    public static void setCdmaDbm(CellSignalStrengthCdma signalStrength, int cdmaDbm) {
        if (signalStrength != null) {
            signalStrength.setCdmaDbm(cdmaDbm);
        }
    }

    public static void setCdmaEcio(CellSignalStrengthCdma signalStrength, int cdmaEcio) {
        if (signalStrength != null) {
            signalStrength.setCdmaEcio(cdmaEcio);
        }
    }

    public static void setEvdoDbm(CellSignalStrengthCdma signalStrength, int evdoDbm) {
        if (signalStrength != null) {
            signalStrength.setEvdoDbm(evdoDbm);
        }
    }

    public static void setEvdoEcio(CellSignalStrengthCdma signalStrength, int evdoEcio) {
        if (signalStrength != null) {
            signalStrength.setEvdoEcio(evdoEcio);
        }
    }

    public static void setEvdoSnr(CellSignalStrengthCdma signalStrength, int evdoSnr) {
        if (signalStrength != null) {
            signalStrength.setEvdoSnr(evdoSnr);
        }
    }

    public static int getPhoneId(CellSignalStrengthCdma signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getPhoneId();
        }
        return -1;
    }
}
