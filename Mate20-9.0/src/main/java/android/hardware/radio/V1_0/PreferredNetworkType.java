package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class PreferredNetworkType {
    public static final int CDMA_EVDO_AUTO = 4;
    public static final int CDMA_ONLY = 5;
    public static final int EVDO_ONLY = 6;
    public static final int GSM_ONLY = 1;
    public static final int GSM_WCDMA = 0;
    public static final int GSM_WCDMA_AUTO = 3;
    public static final int GSM_WCDMA_CDMA_EVDO_AUTO = 7;
    public static final int LTE_CDMA_EVDO = 8;
    public static final int LTE_CMDA_EVDO_GSM_WCDMA = 10;
    public static final int LTE_GSM_WCDMA = 9;
    public static final int LTE_ONLY = 11;
    public static final int LTE_WCDMA = 12;
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
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
