package android.hardware.usb.V1_0;

import java.util.ArrayList;

public final class PortDataRole {
    public static final int DEVICE = 2;
    public static final int HOST = 1;
    public static final int NONE = 0;
    public static final int NUM_DATA_ROLES = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "HOST";
        }
        if (o == 2) {
            return "DEVICE";
        }
        if (o == 3) {
            return "NUM_DATA_ROLES";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("HOST");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("DEVICE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("NUM_DATA_ROLES");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
