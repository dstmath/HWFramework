package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class RttType {
    public static final int ONE_SIDED = 1;
    public static final int TWO_SIDED = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "ONE_SIDED";
        }
        if (o == 2) {
            return "TWO_SIDED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("ONE_SIDED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TWO_SIDED");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
