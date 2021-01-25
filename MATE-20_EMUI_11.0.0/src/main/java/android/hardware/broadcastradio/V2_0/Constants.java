package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class Constants {
    public static final int ANTENNA_DISCONNECTED_TIMEOUT_MS = 100;
    public static final int INVALID_IMAGE = 0;
    public static final int LIST_COMPLETE_TIMEOUT_MS = 300000;

    public static final String toString(int o) {
        if (o == 0) {
            return "INVALID_IMAGE";
        }
        if (o == 100) {
            return "ANTENNA_DISCONNECTED_TIMEOUT_MS";
        }
        if (o == 300000) {
            return "LIST_COMPLETE_TIMEOUT_MS";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("INVALID_IMAGE");
        if ((o & 100) == 100) {
            list.add("ANTENNA_DISCONNECTED_TIMEOUT_MS");
            flipped = 0 | 100;
        }
        if ((o & LIST_COMPLETE_TIMEOUT_MS) == 300000) {
            list.add("LIST_COMPLETE_TIMEOUT_MS");
            flipped |= LIST_COMPLETE_TIMEOUT_MS;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
