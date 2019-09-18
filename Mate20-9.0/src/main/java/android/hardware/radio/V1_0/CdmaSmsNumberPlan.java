package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaSmsNumberPlan {
    public static final int DATA = 3;
    public static final int PRIVATE = 9;
    public static final int RESERVED_10 = 10;
    public static final int RESERVED_11 = 11;
    public static final int RESERVED_12 = 12;
    public static final int RESERVED_13 = 13;
    public static final int RESERVED_14 = 14;
    public static final int RESERVED_15 = 15;
    public static final int RESERVED_2 = 2;
    public static final int RESERVED_5 = 5;
    public static final int RESERVED_6 = 6;
    public static final int RESERVED_7 = 7;
    public static final int RESERVED_8 = 8;
    public static final int TELEPHONY = 1;
    public static final int TELEX = 4;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "TELEPHONY";
        }
        if (o == 2) {
            return "RESERVED_2";
        }
        if (o == 3) {
            return "DATA";
        }
        if (o == 4) {
            return "TELEX";
        }
        if (o == 5) {
            return "RESERVED_5";
        }
        if (o == 6) {
            return "RESERVED_6";
        }
        if (o == 7) {
            return "RESERVED_7";
        }
        if (o == 8) {
            return "RESERVED_8";
        }
        if (o == 9) {
            return "PRIVATE";
        }
        if (o == 10) {
            return "RESERVED_10";
        }
        if (o == 11) {
            return "RESERVED_11";
        }
        if (o == 12) {
            return "RESERVED_12";
        }
        if (o == 13) {
            return "RESERVED_13";
        }
        if (o == 14) {
            return "RESERVED_14";
        }
        if (o == 15) {
            return "RESERVED_15";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("TELEPHONY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("RESERVED_2");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("DATA");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TELEX");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("RESERVED_5");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("RESERVED_6");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("RESERVED_7");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("RESERVED_8");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("PRIVATE");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("RESERVED_10");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("RESERVED_11");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("RESERVED_12");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("RESERVED_13");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("RESERVED_14");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("RESERVED_15");
            flipped |= 15;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
