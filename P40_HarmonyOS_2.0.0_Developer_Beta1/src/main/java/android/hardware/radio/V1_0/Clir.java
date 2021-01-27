package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class Clir {
    public static final int DEFAULT = 0;
    public static final int INVOCATION = 1;
    public static final int SUPPRESSION = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "DEFAULT";
        }
        if (o == 1) {
            return "INVOCATION";
        }
        if (o == 2) {
            return "SUPPRESSION";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DEFAULT");
        if ((o & 1) == 1) {
            list.add("INVOCATION");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SUPPRESSION");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
