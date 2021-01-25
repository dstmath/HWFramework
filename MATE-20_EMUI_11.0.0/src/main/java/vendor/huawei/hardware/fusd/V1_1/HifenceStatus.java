package vendor.huawei.hardware.fusd.V1_1;

import java.util.ArrayList;

public final class HifenceStatus {
    public static final int CELLFENCE = 4;
    public static final int GEOFENCE = 2;
    public static final int HITRACK = 1;

    public static final String toString(int o) {
        if (o == 1) {
            return "HITRACK";
        }
        if (o == 2) {
            return "GEOFENCE";
        }
        if (o == 4) {
            return "CELLFENCE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("HITRACK");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("GEOFENCE");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("CELLFENCE");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
