package android.hardware.vibrator.V1_0;

import java.util.ArrayList;

public final class Effect {
    public static final int CLICK = 0;
    public static final int DOUBLE_CLICK = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "CLICK";
        }
        if (o == 1) {
            return "DOUBLE_CLICK";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("CLICK");
        if ((o & 1) == 1) {
            list.add("DOUBLE_CLICK");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
