package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class TimeStampType {
    public static final int ANTENNA = 1;
    public static final int JAVA_RIL = 4;
    public static final int MODEM = 2;
    public static final int OEM_RIL = 3;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "ANTENNA";
        }
        if (o == 2) {
            return "MODEM";
        }
        if (o == 3) {
            return "OEM_RIL";
        }
        if (o == 4) {
            return "JAVA_RIL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("ANTENNA");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MODEM");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("OEM_RIL");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("JAVA_RIL");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
