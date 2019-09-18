package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class PinState {
    public static final int DISABLED = 3;
    public static final int ENABLED_BLOCKED = 4;
    public static final int ENABLED_NOT_VERIFIED = 1;
    public static final int ENABLED_PERM_BLOCKED = 5;
    public static final int ENABLED_VERIFIED = 2;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "ENABLED_NOT_VERIFIED";
        }
        if (o == 2) {
            return "ENABLED_VERIFIED";
        }
        if (o == 3) {
            return "DISABLED";
        }
        if (o == 4) {
            return "ENABLED_BLOCKED";
        }
        if (o == 5) {
            return "ENABLED_PERM_BLOCKED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("ENABLED_NOT_VERIFIED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ENABLED_VERIFIED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("DISABLED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ENABLED_BLOCKED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ENABLED_PERM_BLOCKED");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
