package android.hardware.radio.V1_1;

import java.util.ArrayList;

public final class UtranBands {
    public static final int BAND_1 = 1;
    public static final int BAND_10 = 10;
    public static final int BAND_11 = 11;
    public static final int BAND_12 = 12;
    public static final int BAND_13 = 13;
    public static final int BAND_14 = 14;
    public static final int BAND_19 = 19;
    public static final int BAND_2 = 2;
    public static final int BAND_20 = 20;
    public static final int BAND_21 = 21;
    public static final int BAND_22 = 22;
    public static final int BAND_25 = 25;
    public static final int BAND_26 = 26;
    public static final int BAND_3 = 3;
    public static final int BAND_4 = 4;
    public static final int BAND_5 = 5;
    public static final int BAND_6 = 6;
    public static final int BAND_7 = 7;
    public static final int BAND_8 = 8;
    public static final int BAND_9 = 9;

    public static final String toString(int o) {
        if (o == 1) {
            return "BAND_1";
        }
        if (o == 2) {
            return "BAND_2";
        }
        if (o == 3) {
            return "BAND_3";
        }
        if (o == 4) {
            return "BAND_4";
        }
        if (o == 5) {
            return "BAND_5";
        }
        if (o == 6) {
            return "BAND_6";
        }
        if (o == 7) {
            return "BAND_7";
        }
        if (o == 8) {
            return "BAND_8";
        }
        if (o == 9) {
            return "BAND_9";
        }
        if (o == 10) {
            return "BAND_10";
        }
        if (o == 11) {
            return "BAND_11";
        }
        if (o == 12) {
            return "BAND_12";
        }
        if (o == 13) {
            return "BAND_13";
        }
        if (o == 14) {
            return "BAND_14";
        }
        if (o == 19) {
            return "BAND_19";
        }
        if (o == 20) {
            return "BAND_20";
        }
        if (o == 21) {
            return "BAND_21";
        }
        if (o == 22) {
            return "BAND_22";
        }
        if (o == 25) {
            return "BAND_25";
        }
        if (o == 26) {
            return "BAND_26";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("BAND_1");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BAND_2");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("BAND_3");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("BAND_4");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("BAND_5");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("BAND_6");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("BAND_7");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("BAND_8");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("BAND_9");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("BAND_10");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("BAND_11");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("BAND_12");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("BAND_13");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("BAND_14");
            flipped |= 14;
        }
        if ((o & 19) == 19) {
            list.add("BAND_19");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("BAND_20");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("BAND_21");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("BAND_22");
            flipped |= 22;
        }
        if ((o & 25) == 25) {
            list.add("BAND_25");
            flipped |= 25;
        }
        if ((o & 26) == 26) {
            list.add("BAND_26");
            flipped |= 26;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
