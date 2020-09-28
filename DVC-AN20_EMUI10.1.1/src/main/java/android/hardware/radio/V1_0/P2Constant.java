package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class P2Constant {
    public static final int NO_P2 = -1;

    public static final String toString(int o) {
        if (o == -1) {
            return "NO_P2";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & -1) == -1) {
            list.add("NO_P2");
            flipped = 0 | -1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
