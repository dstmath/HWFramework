package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CarrierMatchType {
    public static final int ALL = 0;
    public static final int GID1 = 3;
    public static final int GID2 = 4;
    public static final int IMSI_PREFIX = 2;
    public static final int SPN = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "ALL";
        }
        if (o == 1) {
            return "SPN";
        }
        if (o == 2) {
            return "IMSI_PREFIX";
        }
        if (o == 3) {
            return "GID1";
        }
        if (o == 4) {
            return "GID2";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("ALL");
        if ((o & 1) == 1) {
            list.add("SPN");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("IMSI_PREFIX");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("GID1");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("GID2");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
