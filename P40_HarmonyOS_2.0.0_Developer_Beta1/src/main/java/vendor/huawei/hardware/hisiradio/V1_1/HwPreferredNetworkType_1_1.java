package vendor.huawei.hardware.hisiradio.V1_1;

import java.util.ArrayList;

public final class HwPreferredNetworkType_1_1 {
    public static final int CDMA_EVDO_AUTO = 4;
    public static final int CDMA_EVDO_GSM = 56;
    public static final int CDMA_EVDO_WCDMA = 57;
    public static final int CDMA_GSM = 59;
    public static final int CDMA_HDR_GSM_WCDMA = 52;
    public static final int CDMA_HDR_GSM_WCDMA_LTEFDD = 61;
    public static final int CDMA_LTE_WCDMA_GSM = 63;
    public static final int CDMA_ONLY = 5;
    public static final int CDMA_WCDMA = 62;
    public static final int CDMA_WCDMA_GSM = 55;
    public static final int EVDO_ONLY = 6;
    public static final int GSM_ONLY = 1;
    public static final int GSM_WCDMA = 0;
    public static final int GSM_WCDMA_AUTO = 3;
    public static final int GSM_WCDMA_CDMA_EVDO_AUTO = 7;
    public static final int LTEFDD_ONLY = 31;
    public static final int LTETDD_ONLY = 30;
    public static final int LTE_CDMA_EVDO = 8;
    public static final int LTE_CDMA_EVDO_WCDMA = 58;
    public static final int LTE_CMDA_EVDO_GSM_WCDMA = 10;
    public static final int LTE_GSM_WCDMA = 9;
    public static final int LTE_ONLY = 11;
    public static final int LTE_WCDMA = 12;
    public static final int NR_LTE = 67;
    public static final int NR_LTE_CDMA_EVDO = 64;
    public static final int NR_LTE_CDMA_EVDO_GSM_WCDMA = 69;
    public static final int NR_LTE_GSM_WCDMA = 65;
    public static final int NR_LTE_WCDMA = 68;
    public static final int NR_ONLY = 66;
    public static final int TD_SCDMA_GSM = 16;
    public static final int TD_SCDMA_GSM_LTE = 17;
    public static final int TD_SCDMA_GSM_WCDMA = 18;
    public static final int TD_SCDMA_GSM_WCDMA_CDMA_EVDO_AUTO = 21;
    public static final int TD_SCDMA_GSM_WCDMA_LTE = 20;
    public static final int TD_SCDMA_LTE = 15;
    public static final int TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA = 22;
    public static final int TD_SCDMA_ONLY = 13;
    public static final int TD_SCDMA_WCDMA = 14;
    public static final int TD_SCDMA_WCDMA_LTE = 19;
    public static final int WCDMA = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "GSM_WCDMA";
        }
        if (o == 1) {
            return "GSM_ONLY";
        }
        if (o == 2) {
            return "WCDMA";
        }
        if (o == 3) {
            return "GSM_WCDMA_AUTO";
        }
        if (o == 4) {
            return "CDMA_EVDO_AUTO";
        }
        if (o == 5) {
            return "CDMA_ONLY";
        }
        if (o == 6) {
            return "EVDO_ONLY";
        }
        if (o == 7) {
            return "GSM_WCDMA_CDMA_EVDO_AUTO";
        }
        if (o == 8) {
            return "LTE_CDMA_EVDO";
        }
        if (o == 9) {
            return "LTE_GSM_WCDMA";
        }
        if (o == 10) {
            return "LTE_CMDA_EVDO_GSM_WCDMA";
        }
        if (o == 11) {
            return "LTE_ONLY";
        }
        if (o == 12) {
            return "LTE_WCDMA";
        }
        if (o == 13) {
            return "TD_SCDMA_ONLY";
        }
        if (o == 14) {
            return "TD_SCDMA_WCDMA";
        }
        if (o == 15) {
            return "TD_SCDMA_LTE";
        }
        if (o == 16) {
            return "TD_SCDMA_GSM";
        }
        if (o == 17) {
            return "TD_SCDMA_GSM_LTE";
        }
        if (o == 18) {
            return "TD_SCDMA_GSM_WCDMA";
        }
        if (o == 19) {
            return "TD_SCDMA_WCDMA_LTE";
        }
        if (o == 20) {
            return "TD_SCDMA_GSM_WCDMA_LTE";
        }
        if (o == 21) {
            return "TD_SCDMA_GSM_WCDMA_CDMA_EVDO_AUTO";
        }
        if (o == 22) {
            return "TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA";
        }
        if (o == 30) {
            return "LTETDD_ONLY";
        }
        if (o == 31) {
            return "LTEFDD_ONLY";
        }
        if (o == 52) {
            return "CDMA_HDR_GSM_WCDMA";
        }
        if (o == 55) {
            return "CDMA_WCDMA_GSM";
        }
        if (o == 56) {
            return "CDMA_EVDO_GSM";
        }
        if (o == 57) {
            return "CDMA_EVDO_WCDMA";
        }
        if (o == 58) {
            return "LTE_CDMA_EVDO_WCDMA";
        }
        if (o == 59) {
            return "CDMA_GSM";
        }
        if (o == 61) {
            return "CDMA_HDR_GSM_WCDMA_LTEFDD";
        }
        if (o == 62) {
            return "CDMA_WCDMA";
        }
        if (o == 63) {
            return "CDMA_LTE_WCDMA_GSM";
        }
        if (o == 64) {
            return "NR_LTE_CDMA_EVDO";
        }
        if (o == 65) {
            return "NR_LTE_GSM_WCDMA";
        }
        if (o == 66) {
            return "NR_ONLY";
        }
        if (o == 67) {
            return "NR_LTE";
        }
        if (o == 68) {
            return "NR_LTE_WCDMA";
        }
        if (o == 69) {
            return "NR_LTE_CDMA_EVDO_GSM_WCDMA";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("GSM_WCDMA");
        if ((o & 1) == 1) {
            list.add("GSM_ONLY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("WCDMA");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("GSM_WCDMA_AUTO");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CDMA_EVDO_AUTO");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("CDMA_ONLY");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("EVDO_ONLY");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("GSM_WCDMA_CDMA_EVDO_AUTO");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("LTE_CDMA_EVDO");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("LTE_GSM_WCDMA");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("LTE_CMDA_EVDO_GSM_WCDMA");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("LTE_ONLY");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("LTE_WCDMA");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("TD_SCDMA_ONLY");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("TD_SCDMA_WCDMA");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("TD_SCDMA_LTE");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("TD_SCDMA_GSM");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("TD_SCDMA_GSM_LTE");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("TD_SCDMA_GSM_WCDMA");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("TD_SCDMA_WCDMA_LTE");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("TD_SCDMA_GSM_WCDMA_LTE");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("TD_SCDMA_GSM_WCDMA_CDMA_EVDO_AUTO");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA");
            flipped |= 22;
        }
        if ((o & 30) == 30) {
            list.add("LTETDD_ONLY");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("LTEFDD_ONLY");
            flipped |= 31;
        }
        if ((o & 52) == 52) {
            list.add("CDMA_HDR_GSM_WCDMA");
            flipped |= 52;
        }
        if ((o & 55) == 55) {
            list.add("CDMA_WCDMA_GSM");
            flipped |= 55;
        }
        if ((o & 56) == 56) {
            list.add("CDMA_EVDO_GSM");
            flipped |= 56;
        }
        if ((o & 57) == 57) {
            list.add("CDMA_EVDO_WCDMA");
            flipped |= 57;
        }
        if ((o & 58) == 58) {
            list.add("LTE_CDMA_EVDO_WCDMA");
            flipped |= 58;
        }
        if ((o & 59) == 59) {
            list.add("CDMA_GSM");
            flipped |= 59;
        }
        if ((o & 61) == 61) {
            list.add("CDMA_HDR_GSM_WCDMA_LTEFDD");
            flipped |= 61;
        }
        if ((o & 62) == 62) {
            list.add("CDMA_WCDMA");
            flipped |= 62;
        }
        if ((o & 63) == 63) {
            list.add("CDMA_LTE_WCDMA_GSM");
            flipped |= 63;
        }
        if ((o & 64) == 64) {
            list.add("NR_LTE_CDMA_EVDO");
            flipped |= 64;
        }
        if ((o & 65) == 65) {
            list.add("NR_LTE_GSM_WCDMA");
            flipped |= 65;
        }
        if ((o & 66) == 66) {
            list.add("NR_ONLY");
            flipped |= 66;
        }
        if ((o & 67) == 67) {
            list.add("NR_LTE");
            flipped |= 67;
        }
        if ((o & 68) == 68) {
            list.add("NR_LTE_WCDMA");
            flipped |= 68;
        }
        if ((o & 69) == 69) {
            list.add("NR_LTE_CDMA_EVDO_GSM_WCDMA");
            flipped |= 69;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
