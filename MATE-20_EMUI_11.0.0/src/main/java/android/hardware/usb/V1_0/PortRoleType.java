package android.hardware.usb.V1_0;

import java.util.ArrayList;

public final class PortRoleType {
    public static final int DATA_ROLE = 0;
    public static final int MODE = 2;
    public static final int POWER_ROLE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "DATA_ROLE";
        }
        if (o == 1) {
            return "POWER_ROLE";
        }
        if (o == 2) {
            return "MODE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DATA_ROLE");
        if ((o & 1) == 1) {
            list.add("POWER_ROLE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MODE");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
