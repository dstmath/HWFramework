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
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 0) == 0) {
            list.add("NONE");
            flipped = 0;
        }
        if ((o & 1) == 1) {
            list.add("SHARED_KEY_128_MASK");
            flipped |= 1;
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
