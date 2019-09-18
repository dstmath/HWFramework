package android.hardware.usb.V1_0;

import java.util.ArrayList;

public final class PortPowerRole {
    public static final int NONE = 0;
    public static final int NUM_POWER_ROLES = 3;
    public static final int SINK = 2;
    public static final int SOURCE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "SOURCE";
        }
        if (o == 2) {
            return "SINK";
        }
        if (o == 3) {
            return "NUM_POWER_ROLES";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("SOURCE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SINK");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("NUM_POWER_ROLES");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
