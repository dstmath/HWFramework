package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiBand {
    public static final int BAND_24GHZ = 1;
    public static final int BAND_24GHZ_5GHZ = 3;
    public static final int BAND_24GHZ_5GHZ_WITH_DFS = 7;
    public static final int BAND_5GHZ = 2;
    public static final int BAND_5GHZ_DFS = 4;
    public static final int BAND_5GHZ_WITH_DFS = 6;
    public static final int BAND_UNSPECIFIED = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "BAND_UNSPECIFIED";
        }
        if (o == 1) {
            return "BAND_24GHZ";
        }
        if (o == 2) {
            return "BAND_5GHZ";
        }
        if (o == 4) {
            return "BAND_5GHZ_DFS";
        }
        if (o == 6) {
            return "BAND_5GHZ_WITH_DFS";
        }
        if (o == 3) {
            return "BAND_24GHZ_5GHZ";
        }
        if (o == 7) {
            return "BAND_24GHZ_5GHZ_WITH_DFS";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("BAND_UNSPECIFIED");
        if ((o & 1) == 1) {
            list.add("BAND_24GHZ");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BAND_5GHZ");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("BAND_5GHZ_DFS");
            flipped |= 4;
        }
        if ((o & 6) == 6) {
            list.add("BAND_5GHZ_WITH_DFS");
            flipped |= 6;
        }
        if ((o & 3) == 3) {
            list.add("BAND_24GHZ_5GHZ");
            flipped |= 3;
        }
        if ((o & 7) == 7) {
            list.add("BAND_24GHZ_5GHZ_WITH_DFS");
            flipped |= 7;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
