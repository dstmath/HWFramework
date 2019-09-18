package android.hardware.biometrics.fingerprint.V2_1;

import java.util.ArrayList;

public final class FingerprintMsgType {
    public static final int ACQUIRED = 1;
    public static final int AUTHENTICATED = 5;
    public static final int ERROR = -1;
    public static final int TEMPLATE_ENROLLING = 3;
    public static final int TEMPLATE_ENUMERATING = 6;
    public static final int TEMPLATE_REMOVED = 4;

    public static final String toString(int o) {
        if (o == -1) {
            return "ERROR";
        }
        if (o == 1) {
            return "ACQUIRED";
        }
        if (o == 3) {
            return "TEMPLATE_ENROLLING";
        }
        if (o == 4) {
            return "TEMPLATE_REMOVED";
        }
        if (o == 5) {
            return "AUTHENTICATED";
        }
        if (o == 6) {
            return "TEMPLATE_ENUMERATING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & -1) == -1) {
            list.add("ERROR");
            flipped = 0 | -1;
        }
        if ((o & 1) == 1) {
            list.add("ACQUIRED");
            flipped |= 1;
        }
        if ((o & 3) == 3) {
            list.add("TEMPLATE_ENROLLING");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TEMPLATE_REMOVED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("AUTHENTICATED");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("TEMPLATE_ENUMERATING");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
