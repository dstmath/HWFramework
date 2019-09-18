package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class StaScanLimits {
    public static final int MAX_AP_CACHE_PER_SCAN = 32;
    public static final int MAX_BUCKETS = 16;
    public static final int MAX_CHANNELS = 16;

    public static final String toString(int o) {
        if (o == 16) {
            return "MAX_CHANNELS";
        }
        if (o == 16) {
            return "MAX_BUCKETS";
        }
        if (o == 32) {
            return "MAX_AP_CACHE_PER_SCAN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 16) == 16) {
            list.add("MAX_CHANNELS");
            flipped = 0 | 16;
        }
        if ((o & 16) == 16) {
            list.add("MAX_BUCKETS");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("MAX_AP_CACHE_PER_SCAN");
            flipped |= 32;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
