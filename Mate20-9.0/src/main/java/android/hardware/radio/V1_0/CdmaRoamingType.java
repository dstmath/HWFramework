package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaRoamingType {
    public static final int AFFILIATED_ROAM = 1;
    public static final int ANY_ROAM = 2;
    public static final int HOME_NETWORK = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "HOME_NETWORK";
        }
        if (o == 1) {
            return "AFFILIATED_ROAM";
        }
        if (o == 2) {
            return "ANY_ROAM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("HOME_NETWORK");
        if ((o & 1) == 1) {
            list.add("AFFILIATED_ROAM");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ANY_ROAM");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
