package android.hardware.radio.config.V1_0;

import java.util.ArrayList;

public final class SlotState {
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "INACTIVE";
        }
        if (o == 1) {
            return "ACTIVE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("INACTIVE");
        if ((o & 1) == 1) {
            list.add("ACTIVE");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
