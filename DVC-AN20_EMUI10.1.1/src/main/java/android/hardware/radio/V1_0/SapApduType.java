package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SapApduType {
    public static final int APDU = 0;
    public static final int APDU7816 = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "APDU";
        }
        if (o == 1) {
            return "APDU7816";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("APDU");
        if ((o & 1) == 1) {
            list.add("APDU7816");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
