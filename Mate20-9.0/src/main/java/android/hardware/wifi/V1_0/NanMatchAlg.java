package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanMatchAlg {
    public static final int MATCH_CONTINUOUS = 1;
    public static final int MATCH_NEVER = 2;
    public static final int MATCH_ONCE = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "MATCH_ONCE";
        }
        if (o == 1) {
            return "MATCH_CONTINUOUS";
        }
        if (o == 2) {
            return "MATCH_NEVER";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("MATCH_ONCE");
        if ((o & 1) == 1) {
            list.add("MATCH_CONTINUOUS");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MATCH_NEVER");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
