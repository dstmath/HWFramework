package android.hardware.radio.V1_1;

import java.util.ArrayList;

public final class GeranBands {
    public static final int BAND_450 = 3;
    public static final int BAND_480 = 4;
    public static final int BAND_710 = 5;
    public static final int BAND_750 = 6;
    public static final int BAND_850 = 8;
    public static final int BAND_DCS1800 = 12;
    public static final int BAND_E900 = 10;
    public static final int BAND_ER900 = 14;
    public static final int BAND_P900 = 9;
    public static final int BAND_PCS1900 = 13;
    public static final int BAND_R900 = 11;
    public static final int BAND_T380 = 1;
    public static final int BAND_T410 = 2;
    public static final int BAND_T810 = 7;

    public static final String toString(int o) {
        if (o == 1) {
            return "BAND_T380";
        }
        if (o == 2) {
            return "BAND_T410";
        }
        if (o == 3) {
            return "BAND_450";
        }
        if (o == 4) {
            return "BAND_480";
        }
        if (o == 5) {
            return "BAND_710";
        }
        if (o == 6) {
            return "BAND_750";
        }
        if (o == 7) {
            return "BAND_T810";
        }
        if (o == 8) {
            return "BAND_850";
        }
        if (o == 9) {
            return "BAND_P900";
        }
        if (o == 10) {
            return "BAND_E900";
        }
        if (o == 11) {
            return "BAND_R900";
        }
        if (o == 12) {
            return "BAND_DCS1800";
        }
        if (o == 13) {
            return "BAND_PCS1900";
        }
        if (o == 14) {
            return "BAND_ER900";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("BAND_T380");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BAND_T410");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("BAND_450");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("BAND_480");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("BAND_710");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("BAND_750");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("BAND_T810");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("BAND_850");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("BAND_P900");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("BAND_E900");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("BAND_R900");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("BAND_DCS1800");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("BAND_PCS1900");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("BAND_ER900");
            flipped |= 14;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
