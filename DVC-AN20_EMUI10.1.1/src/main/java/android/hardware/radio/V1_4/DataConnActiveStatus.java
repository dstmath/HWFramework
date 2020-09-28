package android.hardware.radio.V1_4;

import java.util.ArrayList;

public final class DataConnActiveStatus {
    public static final int ACTIVE = 2;
    public static final int DORMANT = 1;
    public static final int INACTIVE = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "INACTIVE";
        }
        if (o == 1) {
            return "DORMANT";
        }
        if (o == 2) {
            return "ACTIVE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("INACTIVE");
        if ((o & 1) == 1) {
            list.add("DORMANT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ACTIVE");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
