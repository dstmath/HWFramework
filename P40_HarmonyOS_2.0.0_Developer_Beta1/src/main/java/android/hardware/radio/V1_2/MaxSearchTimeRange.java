package android.hardware.radio.V1_2;

import java.util.ArrayList;

public final class MaxSearchTimeRange {
    public static final int MAX = 3600;
    public static final int MIN = 60;

    public static final String toString(int o) {
        if (o == 60) {
            return "MIN";
        }
        if (o == 3600) {
            return "MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 60) == 60) {
            list.add("MIN");
            flipped = 0 | 60;
        }
        if ((o & 3600) == 3600) {
            list.add("MAX");
            flipped |= 3600;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
