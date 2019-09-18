package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class PhoneRestrictedState {
    public static final int CS_ALL = 4;
    public static final int CS_EMERGENCY = 1;
    public static final int CS_NORMAL = 2;
    public static final int NONE = 0;
    public static final int PS_ALL = 16;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "CS_EMERGENCY";
        }
        if (o == 2) {
            return "CS_NORMAL";
        }
        if (o == 4) {
            return "CS_ALL";
        }
        if (o == 16) {
            return "PS_ALL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("CS_EMERGENCY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CS_NORMAL");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("CS_ALL");
            flipped |= 4;
        }
        if ((o & 16) == 16) {
            list.add("PS_ALL");
            flipped |= 16;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
