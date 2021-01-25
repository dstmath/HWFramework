package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiDebugRingBufferVerboseLevel {
    public static final int DEFAULT = 1;
    public static final int EXCESSIVE = 3;
    public static final int NONE = 0;
    public static final int VERBOSE = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "DEFAULT";
        }
        if (o == 2) {
            return "VERBOSE";
        }
        if (o == 3) {
            return "EXCESSIVE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("DEFAULT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("VERBOSE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("EXCESSIVE");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
