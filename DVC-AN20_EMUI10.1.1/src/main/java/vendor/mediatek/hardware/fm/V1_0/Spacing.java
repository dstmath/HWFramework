package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Spacing {
    public static final int SPACE100 = 1;
    public static final int SPACE200 = 2;
    public static final int SPACE50 = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "SPACE50";
        }
        if (o == 1) {
            return "SPACE100";
        }
        if (o == 2) {
            return "SPACE200";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SPACE50");
        if ((o & 1) == 1) {
            list.add("SPACE100");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SPACE200");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
