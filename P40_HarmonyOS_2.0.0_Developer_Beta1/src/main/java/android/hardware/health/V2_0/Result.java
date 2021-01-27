package android.hardware.health.V2_0;

import java.util.ArrayList;

public final class Result {
    public static final int CALLBACK_DIED = 4;
    public static final int NOT_FOUND = 3;
    public static final int NOT_SUPPORTED = 1;
    public static final int SUCCESS = 0;
    public static final int UNKNOWN = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "NOT_SUPPORTED";
        }
        if (o == 2) {
            return "UNKNOWN";
        }
        if (o == 3) {
            return "NOT_FOUND";
        }
        if (o == 4) {
            return "CALLBACK_DIED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("NOT_SUPPORTED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("UNKNOWN");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("NOT_FOUND");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CALLBACK_DIED");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
