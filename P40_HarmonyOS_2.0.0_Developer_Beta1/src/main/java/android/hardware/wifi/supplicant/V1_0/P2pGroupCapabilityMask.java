package android.hardware.wifi.supplicant.V1_0;

import java.util.ArrayList;

public final class P2pGroupCapabilityMask {
    public static final int CROSS_CONN = 16;
    public static final int GROUP_FORMATION = 64;
    public static final int GROUP_LIMIT = 4;
    public static final int GROUP_OWNER = 1;
    public static final int INTRA_BSS_DIST = 8;
    public static final int PERSISTENT_GROUP = 2;
    public static final int PERSISTENT_RECONN = 32;

    public static final String toString(int o) {
        if (o == 1) {
            return "GROUP_OWNER";
        }
        if (o == 2) {
            return "PERSISTENT_GROUP";
        }
        if (o == 4) {
            return "GROUP_LIMIT";
        }
        if (o == 8) {
            return "INTRA_BSS_DIST";
        }
        if (o == 16) {
            return "CROSS_CONN";
        }
        if (o == 32) {
            return "PERSISTENT_RECONN";
        }
        if (o == 64) {
            return "GROUP_FORMATION";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("GROUP_OWNER");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("PERSISTENT_GROUP");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("GROUP_LIMIT");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("INTRA_BSS_DIST");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("CROSS_CONN");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("PERSISTENT_RECONN");
            flipped |= 32;
        }
        if ((o & 64) == 64) {
            list.add("GROUP_FORMATION");
            flipped |= 64;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
