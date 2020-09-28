package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Deemphasis {
    public static final int D50 = 1;
    public static final int D75 = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "D50";
        }
        if (o == 2) {
            return "D75";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("D50");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("D75");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
