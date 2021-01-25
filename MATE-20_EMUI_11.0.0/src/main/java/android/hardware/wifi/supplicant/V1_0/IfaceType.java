package android.hardware.wifi.supplicant.V1_0;

import java.util.ArrayList;

public final class IfaceType {
    public static final int P2P = 1;
    public static final int STA = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "STA";
        }
        if (o == 1) {
            return "P2P";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("STA");
        if ((o & 1) == 1) {
            list.add("P2P");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
