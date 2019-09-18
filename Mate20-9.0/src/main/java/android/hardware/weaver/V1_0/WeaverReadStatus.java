package android.hardware.weaver.V1_0;

import java.util.ArrayList;

public final class WeaverReadStatus {
    public static final int FAILED = 1;
    public static final int INCORRECT_KEY = 2;
    public static final int OK = 0;
    public static final int THROTTLE = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "OK";
        }
        if (o == 1) {
            return "FAILED";
        }
        if (o == 2) {
            return "INCORRECT_KEY";
        }
        if (o == 3) {
            return "THROTTLE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OK");
        if ((o & 1) == 1) {
            list.add("FAILED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("INCORRECT_KEY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("THROTTLE");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
