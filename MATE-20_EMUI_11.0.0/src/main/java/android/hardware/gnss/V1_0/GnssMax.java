package android.hardware.gnss.V1_0;

import java.util.ArrayList;

public final class GnssMax {
    public static final int SVS_COUNT = 64;

    public static final String toString(int o) {
        if (o == 64) {
            return "SVS_COUNT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 64) == 64) {
            list.add("SVS_COUNT");
            flipped = 0 | 64;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
