package android.hardware.usb.V1_2;

import java.util.ArrayList;

public final class ContaminantDetectionStatus {
    public static final int DETECTED = 3;
    public static final int DISABLED = 1;
    public static final int NOT_DETECTED = 2;
    public static final int NOT_SUPPORTED = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "NOT_SUPPORTED";
        }
        if (o == 1) {
            return "DISABLED";
        }
        if (o == 2) {
            return "NOT_DETECTED";
        }
        if (o == 3) {
            return "DETECTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NOT_SUPPORTED");
        if ((o & 1) == 1) {
            list.add("DISABLED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NOT_DETECTED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("DETECTED");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
