package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class ResetNvType {
    public static final int ERASE = 1;
    public static final int FACTORY_RESET = 2;
    public static final int RELOAD = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "RELOAD";
        }
        if (o == 1) {
            return "ERASE";
        }
        if (o == 2) {
            return "FACTORY_RESET";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("RELOAD");
        if ((o & 1) == 1) {
            list.add("ERASE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FACTORY_RESET");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
