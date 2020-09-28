package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SsRequestType {
    public static final int ACTIVATION = 0;
    public static final int DEACTIVATION = 1;
    public static final int ERASURE = 4;
    public static final int INTERROGATION = 2;
    public static final int REGISTRATION = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "ACTIVATION";
        }
        if (o == 1) {
            return "DEACTIVATION";
        }
        if (o == 2) {
            return "INTERROGATION";
        }
        if (o == 3) {
            return "REGISTRATION";
        }
        if (o == 4) {
            return "ERASURE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("ACTIVATION");
        if ((o & 1) == 1) {
            list.add("DEACTIVATION");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("INTERROGATION");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("REGISTRATION");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ERASURE");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
