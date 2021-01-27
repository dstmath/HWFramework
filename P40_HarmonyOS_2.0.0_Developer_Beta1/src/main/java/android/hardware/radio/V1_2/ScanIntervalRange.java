package android.hardware.radio.V1_2;

import java.util.ArrayList;

public final class ScanIntervalRange {
    public static final int MAX = 300;
    public static final int MIN = 5;

    public static final String toString(int o) {
        if (o == 5) {
            return "MIN";
        }
        if (o == 300) {
            return "MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 5) == 5) {
            list.add("MIN");
            flipped = 0 | 5;
        }
        if ((o & 300) == 300) {
            list.add("MAX");
            flipped |= 300;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
