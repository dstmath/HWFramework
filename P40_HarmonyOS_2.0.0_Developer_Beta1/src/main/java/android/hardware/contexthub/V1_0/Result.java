package android.hardware.contexthub.V1_0;

import java.util.ArrayList;

public final class Result {
    public static final int BAD_PARAMS = 2;
    public static final int NOT_INIT = 3;
    public static final int OK = 0;
    public static final int TRANSACTION_FAILED = 4;
    public static final int TRANSACTION_PENDING = 5;
    public static final int UNKNOWN_FAILURE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "OK";
        }
        if (o == 1) {
            return "UNKNOWN_FAILURE";
        }
        if (o == 2) {
            return "BAD_PARAMS";
        }
        if (o == 3) {
            return "NOT_INIT";
        }
        if (o == 4) {
            return "TRANSACTION_FAILED";
        }
        if (o == 5) {
            return "TRANSACTION_PENDING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OK");
        if ((o & 1) == 1) {
            list.add("UNKNOWN_FAILURE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BAD_PARAMS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("NOT_INIT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TRANSACTION_FAILED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TRANSACTION_PENDING");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
