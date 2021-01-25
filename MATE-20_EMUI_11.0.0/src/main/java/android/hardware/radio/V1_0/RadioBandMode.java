package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioBandMode {
    public static final int BAND_MODE_10_800M_2 = 15;
    public static final int BAND_MODE_5_450M = 10;
    public static final int BAND_MODE_7_700M_2 = 12;
    public static final int BAND_MODE_8_1800M = 13;
    public static final int BAND_MODE_9_900M = 14;
    public static final int BAND_MODE_AUS = 4;
    public static final int BAND_MODE_AUS_2 = 5;
    public static final int BAND_MODE_AWS = 17;
    public static final int BAND_MODE_CELL_800 = 6;
    public static final int BAND_MODE_EURO = 1;
    public static final int BAND_MODE_EURO_PAMR_400M = 16;
    public static final int BAND_MODE_IMT2000 = 11;
    public static final int BAND_MODE_JPN = 3;
    public static final int BAND_MODE_JTACS = 8;
    public static final int BAND_MODE_KOREA_PCS = 9;
    public static final int BAND_MODE_PCS = 7;
    public static final int BAND_MODE_UNSPECIFIED = 0;
    public static final int BAND_MODE_USA = 2;
    public static final int BAND_MODE_USA_2500M = 18;

    public static final String toString(int o) {
        if (o == 0) {
            return "BAND_MODE_UNSPECIFIED";
        }
        if (o == 1) {
            return "BAND_MODE_EURO";
        }
        if (o == 2) {
            return "BAND_MODE_USA";
        }
        if (o == 3) {
            return "BAND_MODE_JPN";
        }
        if (o == 4) {
            return "BAND_MODE_AUS";
        }
        if (o == 5) {
            return "BAND_MODE_AUS_2";
        }
        if (o == 6) {
            return "BAND_MODE_CELL_800";
        }
        if (o == 7) {
            return "BAND_MODE_PCS";
        }
        if (o == 8) {
            return "BAND_MODE_JTACS";
        }
        if (o == 9) {
            return "BAND_MODE_KOREA_PCS";
        }
        if (o == 10) {
            return "BAND_MODE_5_450M";
        }
        if (o == 11) {
            return "BAND_MODE_IMT2000";
        }
        if (o == 12) {
            return "BAND_MODE_7_700M_2";
        }
        if (o == 13) {
            return "BAND_MODE_8_1800M";
        }
        if (o == 14) {
            return "BAND_MODE_9_900M";
        }
        if (o == 15) {
            return "BAND_MODE_10_800M_2";
        }
        if (o == 16) {
            return "BAND_MODE_EURO_PAMR_400M";
        }
        if (o == 17) {
            return "BAND_MODE_AWS";
        }
        if (o == 18) {
            return "BAND_MODE_USA_2500M";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("BAND_MODE_UNSPECIFIED");
        if ((o & 1) == 1) {
            list.add("BAND_MODE_EURO");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BAND_MODE_USA");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("BAND_MODE_JPN");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("BAND_MODE_AUS");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("BAND_MODE_AUS_2");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("BAND_MODE_CELL_800");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("BAND_MODE_PCS");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("BAND_MODE_JTACS");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("BAND_MODE_KOREA_PCS");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("BAND_MODE_5_450M");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("BAND_MODE_IMT2000");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("BAND_MODE_7_700M_2");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("BAND_MODE_8_1800M");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("BAND_MODE_9_900M");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("BAND_MODE_10_800M_2");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("BAND_MODE_EURO_PAMR_400M");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("BAND_MODE_AWS");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("BAND_MODE_USA_2500M");
            flipped |= 18;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
