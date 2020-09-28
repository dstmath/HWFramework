package android.hardware.radio.V1_4;

import java.util.ArrayList;

public final class FrequencyRange {
    public static final int HIGH = 3;
    public static final int LOW = 1;
    public static final int MID = 2;
    public static final int MMWAVE = 4;

    public static final String toString(int o) {
        if (o == 1) {
            return "LOW";
        }
        if (o == 2) {
            return "MID";
        }
        if (o == 3) {
            return "HIGH";
        }
        if (o == 4) {
            return "MMWAVE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("LOW");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MID");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("HIGH");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("MMWAVE");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
