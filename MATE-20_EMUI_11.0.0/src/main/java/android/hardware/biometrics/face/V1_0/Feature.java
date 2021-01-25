package android.hardware.biometrics.face.V1_0;

import java.util.ArrayList;

public final class Feature {
    public static final int REQUIRE_ATTENTION = 1;
    public static final int REQUIRE_DIVERSITY = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "REQUIRE_ATTENTION";
        }
        if (o == 2) {
            return "REQUIRE_DIVERSITY";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("REQUIRE_ATTENTION");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("REQUIRE_DIVERSITY");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
