package android.hardware.vibrator.V1_0;

import java.util.ArrayList;

public final class Status {
    public static final int BAD_VALUE = 2;
    public static final int OK = 0;
    public static final int UNKNOWN_ERROR = 1;
    public static final int UNSUPPORTED_OPERATION = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "OK";
        }
        if (o == 1) {
            return "UNKNOWN_ERROR";
        }
        if (o == 2) {
            return "BAD_VALUE";
        }
        if (o == 3) {
            return "UNSUPPORTED_OPERATION";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OK");
        if ((o & 1) == 1) {
            list.add("UNKNOWN_ERROR");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BAD_VALUE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("UNSUPPORTED_OPERATION");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
