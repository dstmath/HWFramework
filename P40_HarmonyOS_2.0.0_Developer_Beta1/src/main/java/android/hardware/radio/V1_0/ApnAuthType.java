package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class ApnAuthType {
    public static final int NO_PAP_CHAP = 2;
    public static final int NO_PAP_NO_CHAP = 0;
    public static final int PAP_CHAP = 3;
    public static final int PAP_NO_CHAP = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NO_PAP_NO_CHAP";
        }
        if (o == 1) {
            return "PAP_NO_CHAP";
        }
        if (o == 2) {
            return "NO_PAP_CHAP";
        }
        if (o == 3) {
            return "PAP_CHAP";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NO_PAP_NO_CHAP");
        if ((o & 1) == 1) {
            list.add("PAP_NO_CHAP");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NO_PAP_CHAP");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("PAP_CHAP");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
