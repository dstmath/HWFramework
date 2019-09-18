package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class StaScanDataFlagMask {
    public static final int INTERRUPTED = 1;

    public static final String toString(int o) {
        if (o == 1) {
            return "INTERRUPTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("INTERRUPTED");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
