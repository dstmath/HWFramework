package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SmsAcknowledgeFailCause {
    public static final int MEMORY_CAPACITY_EXCEEDED = 211;
    public static final int UNSPECIFIED_ERROR = 255;

    public static final String toString(int o) {
        if (o == 211) {
            return "MEMORY_CAPACITY_EXCEEDED";
        }
        if (o == 255) {
            return "UNSPECIFIED_ERROR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 211) == 211) {
            list.add("MEMORY_CAPACITY_EXCEEDED");
            flipped = 0 | 211;
        }
        if ((o & 255) == 255) {
            list.add("UNSPECIFIED_ERROR");
            flipped |= 255;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
