package android.hardware.radio.V1_1;

import java.util.ArrayList;

public final class ScanType {
    public static final int ONE_SHOT = 0;
    public static final int PERIODIC = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "ONE_SHOT";
        }
        if (o == 1) {
            return "PERIODIC";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("ONE_SHOT");
        if ((o & 1) == 1) {
            list.add("PERIODIC");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
