package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class RttBw {
    public static final int BW_10MHZ = 2;
    public static final int BW_160MHZ = 32;
    public static final int BW_20MHZ = 4;
    public static final int BW_40MHZ = 8;
    public static final int BW_5MHZ = 1;
    public static final int BW_80MHZ = 16;

    public static final String toString(int o) {
        if (o == 1) {
            return "BW_5MHZ";
        }
        if (o == 2) {
            return "BW_10MHZ";
        }
        if (o == 4) {
            return "BW_20MHZ";
        }
        if (o == 8) {
            return "BW_40MHZ";
        }
        if (o == 16) {
            return "BW_80MHZ";
        }
        if (o == 32) {
            return "BW_160MHZ";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("BW_5MHZ");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BW_10MHZ");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("BW_20MHZ");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("BW_40MHZ");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("BW_80MHZ");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("BW_160MHZ");
            flipped |= 32;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
