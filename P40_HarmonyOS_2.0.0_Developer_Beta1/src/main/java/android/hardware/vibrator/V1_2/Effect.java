package android.hardware.vibrator.V1_2;

import java.util.ArrayList;

public final class Effect {
    public static final int CLICK = 0;
    public static final int DOUBLE_CLICK = 1;
    public static final int HEAVY_CLICK = 5;
    public static final int POP = 4;
    public static final int RINGTONE_1 = 6;
    public static final int RINGTONE_10 = 15;
    public static final int RINGTONE_11 = 16;
    public static final int RINGTONE_12 = 17;
    public static final int RINGTONE_13 = 18;
    public static final int RINGTONE_14 = 19;
    public static final int RINGTONE_15 = 20;
    public static final int RINGTONE_2 = 7;
    public static final int RINGTONE_3 = 8;
    public static final int RINGTONE_4 = 9;
    public static final int RINGTONE_5 = 10;
    public static final int RINGTONE_6 = 11;
    public static final int RINGTONE_7 = 12;
    public static final int RINGTONE_8 = 13;
    public static final int RINGTONE_9 = 14;
    public static final int THUD = 3;
    public static final int TICK = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "CLICK";
        }
        if (o == 1) {
            return "DOUBLE_CLICK";
        }
        if (o == 2) {
            return "TICK";
        }
        if (o == 3) {
            return "THUD";
        }
        if (o == 4) {
            return "POP";
        }
        if (o == 5) {
            return "HEAVY_CLICK";
        }
        if (o == 6) {
            return "RINGTONE_1";
        }
        if (o == 7) {
            return "RINGTONE_2";
        }
        if (o == 8) {
            return "RINGTONE_3";
        }
        if (o == 9) {
            return "RINGTONE_4";
        }
        if (o == 10) {
            return "RINGTONE_5";
        }
        if (o == 11) {
            return "RINGTONE_6";
        }
        if (o == 12) {
            return "RINGTONE_7";
        }
        if (o == 13) {
            return "RINGTONE_8";
        }
        if (o == 14) {
            return "RINGTONE_9";
        }
        if (o == 15) {
            return "RINGTONE_10";
        }
        if (o == 16) {
            return "RINGTONE_11";
        }
        if (o == 17) {
            return "RINGTONE_12";
        }
        if (o == 18) {
            return "RINGTONE_13";
        }
        if (o == 19) {
            return "RINGTONE_14";
        }
        if (o == 20) {
            return "RINGTONE_15";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("CLICK");
        if ((o & 1) == 1) {
            list.add("DOUBLE_CLICK");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TICK");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("THUD");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("POP");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("HEAVY_CLICK");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("RINGTONE_1");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("RINGTONE_2");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("RINGTONE_3");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("RINGTONE_4");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("RINGTONE_5");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("RINGTONE_6");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("RINGTONE_7");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("RINGTONE_8");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("RINGTONE_9");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("RINGTONE_10");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("RINGTONE_11");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("RINGTONE_12");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("RINGTONE_13");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("RINGTONE_14");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("RINGTONE_15");
            flipped |= 20;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
