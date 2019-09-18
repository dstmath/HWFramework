package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioCapabilityStatus {
    public static final int FAIL = 2;
    public static final int NONE = 0;
    public static final int SUCCESS = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "SUCCESS";
        }
        if (o == 2) {
            return "FAIL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("SUCCESS");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FAIL");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
