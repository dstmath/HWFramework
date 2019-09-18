package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaCallWaitingNumberType {
    public static final int INTERNATIONAL = 1;
    public static final int NATIONAL = 2;
    public static final int NETWORK_SPECIFIC = 3;
    public static final int SUBSCRIBER = 4;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "INTERNATIONAL";
        }
        if (o == 2) {
            return "NATIONAL";
        }
        if (o == 3) {
            return "NETWORK_SPECIFIC";
        }
        if (o == 4) {
            return "SUBSCRIBER";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("INTERNATIONAL");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NATIONAL");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("NETWORK_SPECIFIC");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SUBSCRIBER");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
