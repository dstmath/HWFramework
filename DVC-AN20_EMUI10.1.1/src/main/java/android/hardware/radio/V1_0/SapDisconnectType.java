package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SapDisconnectType {
    public static final int GRACEFUL = 0;
    public static final int IMMEDIATE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "GRACEFUL";
        }
        if (o == 1) {
            return "IMMEDIATE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("GRACEFUL");
        if ((o & 1) == 1) {
            list.add("IMMEDIATE");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
