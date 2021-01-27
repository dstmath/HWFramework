package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class UusType {
    public static final int TYPE1_IMPLICIT = 0;
    public static final int TYPE1_NOT_REQUIRED = 2;
    public static final int TYPE1_REQUIRED = 1;
    public static final int TYPE2_NOT_REQUIRED = 4;
    public static final int TYPE2_REQUIRED = 3;
    public static final int TYPE3_NOT_REQUIRED = 6;
    public static final int TYPE3_REQUIRED = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "TYPE1_IMPLICIT";
        }
        if (o == 1) {
            return "TYPE1_REQUIRED";
        }
        if (o == 2) {
            return "TYPE1_NOT_REQUIRED";
        }
        if (o == 3) {
            return "TYPE2_REQUIRED";
        }
        if (o == 4) {
            return "TYPE2_NOT_REQUIRED";
        }
        if (o == 5) {
            return "TYPE3_REQUIRED";
        }
        if (o == 6) {
            return "TYPE3_NOT_REQUIRED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("TYPE1_IMPLICIT");
        if ((o & 1) == 1) {
            list.add("TYPE1_REQUIRED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TYPE1_NOT_REQUIRED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TYPE2_REQUIRED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TYPE2_NOT_REQUIRED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TYPE3_REQUIRED");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("TYPE3_NOT_REQUIRED");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
