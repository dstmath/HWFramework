package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaSmsSubaddressType {
    public static final int NSAP = 0;
    public static final int USER_SPECIFIED = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NSAP";
        }
        if (o == 1) {
            return "USER_SPECIFIED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 0) == 0) {
            list.add("NSAP");
            flipped = 0;
        }
        if ((o & 1) == 1) {
            list.add("USER_SPECIFIED");
            flipped |= 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
