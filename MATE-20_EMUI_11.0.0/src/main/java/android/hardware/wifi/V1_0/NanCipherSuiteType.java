package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanCipherSuiteType {
    public static final int NONE = 0;
    public static final int SHARED_KEY_128_MASK = 1;
    public static final int SHARED_KEY_256_MASK = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "SHARED_KEY_128_MASK";
        }
        if (o == 2) {
            return "SHARED_KEY_256_MASK";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("SHARED_KEY_128_MASK");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SHARED_KEY_256_MASK");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
