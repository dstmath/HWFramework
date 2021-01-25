package android.hardware.radio.V1_2;

import java.util.ArrayList;

public final class AudioQuality {
    public static final int AMR = 1;
    public static final int AMR_WB = 2;
    public static final int EVRC = 6;
    public static final int EVRC_B = 7;
    public static final int EVRC_NW = 9;
    public static final int EVRC_WB = 8;
    public static final int GSM_EFR = 3;
    public static final int GSM_FR = 4;
    public static final int GSM_HR = 5;
    public static final int UNSPECIFIED = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNSPECIFIED";
        }
        if (o == 1) {
            return "AMR";
        }
        if (o == 2) {
            return "AMR_WB";
        }
        if (o == 3) {
            return "GSM_EFR";
        }
        if (o == 4) {
            return "GSM_FR";
        }
        if (o == 5) {
            return "GSM_HR";
        }
        if (o == 6) {
            return "EVRC";
        }
        if (o == 7) {
            return "EVRC_B";
        }
        if (o == 8) {
            return "EVRC_WB";
        }
        if (o == 9) {
            return "EVRC_NW";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNSPECIFIED");
        if ((o & 1) == 1) {
            list.add("AMR");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("AMR_WB");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("GSM_EFR");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("GSM_FR");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("GSM_HR");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("EVRC");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("EVRC_B");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("EVRC_WB");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("EVRC_NW");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
