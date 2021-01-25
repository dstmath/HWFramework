package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class UiccSubActStatus {
    public static final int ACTIVATE = 1;
    public static final int DEACTIVATE = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "DEACTIVATE";
        }
        if (o == 1) {
            return "ACTIVATE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DEACTIVATE");
        if ((o & 1) == 1) {
            list.add("ACTIVATE");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
