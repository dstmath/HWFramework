package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaCallWaitingNumberPlan {
    public static final int DATA = 3;
    public static final int ISDN = 1;
    public static final int NATIONAL = 8;
    public static final int PRIVATE = 9;
    public static final int TELEX = 4;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "ISDN";
        }
        if (o == 3) {
            return "DATA";
        }
        if (o == 4) {
            return "TELEX";
        }
        if (o == 8) {
            return "NATIONAL";
        }
        if (o == 9) {
            return "PRIVATE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("ISDN");
            flipped = 0 | 1;
        }
        if ((o & 3) == 3) {
            list.add("DATA");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TELEX");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("NATIONAL");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("PRIVATE");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
