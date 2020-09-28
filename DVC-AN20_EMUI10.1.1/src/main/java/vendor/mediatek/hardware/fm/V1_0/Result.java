package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Result {
    public static final int FAILED = 1;
    public static final int INVALID_ARGUMENTS = 3;
    public static final int INVALID_STATE = 4;
    public static final int NOT_INITIALIZED = 2;
    public static final int OK = 0;
    public static final int TIMEOUT = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "OK";
        }
        if (o == 1) {
            return "FAILED";
        }
        if (o == 2) {
            return "NOT_INITIALIZED";
        }
        if (o == 3) {
            return "INVALID_ARGUMENTS";
        }
        if (o == 4) {
            return "INVALID_STATE";
        }
        if (o == 5) {
            return "TIMEOUT";
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
            list.add("NOT_INITIALIZED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("INVALID_ARGUMENTS");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("INVALID_STATE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TIMEOUT");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
