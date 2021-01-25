package android.hardware.contexthub.V1_0;

import java.util.ArrayList;

public final class NanoAppFlags {
    public static final int ENCRYPTED = 2;
    public static final int SIGNED = 1;

    public static final String toString(int o) {
        if (o == 1) {
            return "SIGNED";
        }
        if (o == 2) {
            return "ENCRYPTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("SIGNED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ENCRYPTED");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
