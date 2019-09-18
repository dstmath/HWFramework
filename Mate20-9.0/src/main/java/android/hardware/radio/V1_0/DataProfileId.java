package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class DataProfileId {
    public static final int CBS = 4;
    public static final int DEFAULT = 0;
    public static final int FOTA = 3;
    public static final int IMS = 2;
    public static final int INVALID = -1;
    public static final int OEM_BASE = 1000;
    public static final int TETHERED = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "DEFAULT";
        }
        if (o == 1) {
            return "TETHERED";
        }
        if (o == 2) {
            return "IMS";
        }
        if (o == 3) {
            return "FOTA";
        }
        if (o == 4) {
            return "CBS";
        }
        if (o == 1000) {
            return "OEM_BASE";
        }
        if (o == -1) {
            return "INVALID";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DEFAULT");
        if ((o & 1) == 1) {
            list.add("TETHERED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("IMS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("FOTA");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CBS");
            flipped |= 4;
        }
        if ((o & 1000) == 1000) {
            list.add("OEM_BASE");
            flipped |= 1000;
        }
        if ((o & -1) == -1) {
            list.add("INVALID");
            flipped |= -1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
