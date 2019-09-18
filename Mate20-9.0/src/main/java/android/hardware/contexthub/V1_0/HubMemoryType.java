package android.hardware.contexthub.V1_0;

import java.util.ArrayList;

public final class HubMemoryType {
    public static final int MAIN = 0;
    public static final int SECONDARY = 1;
    public static final int TCM = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "MAIN";
        }
        if (o == 1) {
            return "SECONDARY";
        }
        if (o == 2) {
            return "TCM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("MAIN");
        if ((o & 1) == 1) {
            list.add("SECONDARY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TCM");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
