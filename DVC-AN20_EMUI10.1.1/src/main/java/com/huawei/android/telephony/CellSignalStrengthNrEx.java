package com.huawei.android.telephony;

import android.telephony.CellSignalStrengthNr;
import android.telephony.SignalStrength;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CellSignalStrengthNrEx {
    public static final int NR_CSI_RSRP_TYPE = 0;
    public static final int NR_CSI_RSRQ_TYPE = 1;
    public static final int NR_CSI_SINR_TYPE = 2;
    public static final int NR_SS_RSRP_TYPE = 3;
    public static final int NR_SS_RSRQ_TYPE = 4;
    public static final int NR_SS_SINR_TYPE = 5;
    private static final String TAG = "CellSignalStrengthNrEx";
    private CellSignalStrengthNr mCellSignalStrengthNr;

    public void setCellSignalStrengthNr(CellSignalStrengthNr cellSignalStrengthNr) {
        this.mCellSignalStrengthNr = cellSignalStrengthNr;
    }

    public CellSignalStrengthNr getCellSignalStrengthNr() {
        return this.mCellSignalStrengthNr;
    }

    public int getPhoneId() {
        CellSignalStrengthNr cellSignalStrengthNr = this.mCellSignalStrengthNr;
        if (cellSignalStrengthNr != null) {
            return cellSignalStrengthNr.getPhoneId();
        }
        return -1;
    }

    public int getSsRsrp() {
        CellSignalStrengthNr cellSignalStrengthNr = this.mCellSignalStrengthNr;
        if (cellSignalStrengthNr != null) {
            return cellSignalStrengthNr.getSsRsrp();
        }
        return SignalStrengthEx.INVALID;
    }

    public int getSsRsrq() {
        CellSignalStrengthNr cellSignalStrengthNr = this.mCellSignalStrengthNr;
        if (cellSignalStrengthNr != null) {
            return cellSignalStrengthNr.getSsRsrq();
        }
        return SignalStrengthEx.INVALID;
    }

    public int getSsSinr() {
        CellSignalStrengthNr cellSignalStrengthNr = this.mCellSignalStrengthNr;
        if (cellSignalStrengthNr != null) {
            return cellSignalStrengthNr.getSsSinr();
        }
        return SignalStrengthEx.INVALID;
    }

    public static int getLevelHw(SignalStrength signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getCellSignalStrengthNr().getLevelHw();
        }
        return 0;
    }

    public static void setNrSignalStrength(CellSignalStrengthNr signalStrength, int type, int ssValue) {
        if (signalStrength == null) {
            return;
        }
        if (type == 0) {
            signalStrength.setCsiRsrp(ssValue);
        } else if (type == 1) {
            signalStrength.setCsiRsrq(ssValue);
        } else if (type == 2) {
            signalStrength.setCsiSinr(ssValue);
        } else if (type == 3) {
            signalStrength.setNrRsrp(ssValue);
        } else if (type == 4) {
            signalStrength.setNrRsrq(ssValue);
        } else if (type != 5) {
            Log.i(TAG, "unknow type " + type);
        } else {
            signalStrength.setNrRssnr(ssValue);
        }
    }
}
