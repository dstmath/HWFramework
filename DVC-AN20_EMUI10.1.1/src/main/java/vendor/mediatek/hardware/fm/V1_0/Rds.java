package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Rds {
    public static final int NONE = 0;
    public static final int US = 2;
    public static final int WORLD = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "WORLD";
        }
        if (o == 2) {
            return "US";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("WORLD");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("US");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
