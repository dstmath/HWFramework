package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanSrfType {
    public static final int BLOOM_FILTER = 0;
    public static final int PARTIAL_MAC_ADDR = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "BLOOM_FILTER";
        }
        if (o == 1) {
            return "PARTIAL_MAC_ADDR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("BLOOM_FILTER");
        if ((o & 1) == 1) {
            list.add("PARTIAL_MAC_ADDR");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
