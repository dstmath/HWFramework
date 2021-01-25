package android.hardware.radio.V1_1;

import java.util.ArrayList;

public final class KeepaliveType {
    public static final int NATT_IPV4 = 0;
    public static final int NATT_IPV6 = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NATT_IPV4";
        }
        if (o == 1) {
            return "NATT_IPV6";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NATT_IPV4");
        if ((o & 1) == 1) {
            list.add("NATT_IPV6");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
