package android.hardware.radio.V1_2;

import java.util.ArrayList;

public final class IndicationFilter {
    public static final int ALL = -1;
    public static final int DATA_CALL_DORMANCY_CHANGED = 4;
    public static final int FULL_NETWORK_STATE = 2;
    public static final int LINK_CAPACITY_ESTIMATE = 8;
    public static final int NONE = 0;
    public static final int PHYSICAL_CHANNEL_CONFIG = 16;
    public static final int SIGNAL_STRENGTH = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == -1) {
            return "ALL";
        }
        if (o == 1) {
            return "SIGNAL_STRENGTH";
        }
        if (o == 2) {
            return "FULL_NETWORK_STATE";
        }
        if (o == 4) {
            return "DATA_CALL_DORMANCY_CHANGED";
        }
        if (o == 8) {
            return "LINK_CAPACITY_ESTIMATE";
        }
        if (o == 16) {
            return "PHYSICAL_CHANNEL_CONFIG";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & -1) == -1) {
            list.add("ALL");
            flipped = 0 | -1;
        }
        if ((o & 1) == 1) {
            list.add("SIGNAL_STRENGTH");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("FULL_NETWORK_STATE");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("DATA_CALL_DORMANCY_CHANGED");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("LINK_CAPACITY_ESTIMATE");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("PHYSICAL_CHANNEL_CONFIG");
            flipped |= 16;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
