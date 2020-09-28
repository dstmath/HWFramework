package com.huawei.android.telephony;

import android.telephony.CellSignalStrengthLte;
import android.telephony.SignalStrength;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CellSignalStrengthLteEx {
    public static boolean isCdma(CellSignalStrengthLte signalStrength) {
        if (signalStrength != null) {
            return signalStrength.isCdma();
        }
        return false;
    }

    public static void setRssi(CellSignalStrengthLte signalStrength, int rssi) {
        if (signalStrength != null) {
            signalStrength.setRssi(rssi);
        }
    }

    public static void setRsrp(CellSignalStrengthLte signalStrength, int rsrp) {
        if (signalStrength != null) {
            signalStrength.setRsrp(rsrp);
        }
    }

    public static void setRsrq(CellSignalStrengthLte signalStrength, int rsrq) {
        if (signalStrength != null) {
            signalStrength.setRsrq(rsrq);
        }
    }

    public static void setRssnr(CellSignalStrengthLte signalStrength, int rssnr) {
        if (signalStrength != null) {
            signalStrength.setRssnr(rssnr);
        }
    }

    public static void setCqi(CellSignalStrengthLte signalStrength, int cqi) {
        if (signalStrength != null) {
            signalStrength.setCqi(cqi);
        }
    }

    public static void setTimingAdvance(CellSignalStrengthLte signalStrength, int timingAdvance) {
        if (signalStrength != null) {
            signalStrength.setTimingAdvance(timingAdvance);
        }
    }

    public static int getPhoneId(CellSignalStrengthLte signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getPhoneId();
        }
        return -1;
    }

    public static int getLevelHw(SignalStrength signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getCellSignalStrengthLte().getLevelHw();
        }
        return 0;
    }
}
