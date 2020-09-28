package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Region {
    public static final int JAPAN = 1;
    public static final int JAPANW = 2;
    public static final int SPECIAL = 3;
    public static final int UE = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UE";
        }
        if (o == 1) {
            return "JAPAN";
        }
        if (o == 2) {
            return "JAPANW";
        }
        if (o == 3) {
            return "SPECIAL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UE");
        if ((o & 1) == 1) {
            list.add("JAPAN");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("JAPANW");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SPECIAL");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
