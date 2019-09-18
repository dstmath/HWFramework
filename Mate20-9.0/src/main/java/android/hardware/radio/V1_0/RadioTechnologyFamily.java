package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioTechnologyFamily {
    public static final int THREE_GPP = 0;
    public static final int THREE_GPP2 = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "THREE_GPP";
        }
        if (o == 1) {
            return "THREE_GPP2";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("THREE_GPP");
        if ((o & 1) == 1) {
            list.add("THREE_GPP2");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
