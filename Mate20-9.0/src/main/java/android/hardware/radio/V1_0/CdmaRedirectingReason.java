package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaRedirectingReason {
    public static final int CALLED_DTE_OUT_OF_ORDER = 9;
    public static final int CALL_FORWARDING_BUSY = 1;
    public static final int CALL_FORWARDING_BY_THE_CALLED_DTE = 10;
    public static final int CALL_FORWARDING_NO_REPLY = 2;
    public static final int CALL_FORWARDING_UNCONDITIONAL = 15;
    public static final int RESERVED = 16;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "CALL_FORWARDING_BUSY";
        }
        if (o == 2) {
            return "CALL_FORWARDING_NO_REPLY";
        }
        if (o == 9) {
            return "CALLED_DTE_OUT_OF_ORDER";
        }
        if (o == 10) {
            return "CALL_FORWARDING_BY_THE_CALLED_DTE";
        }
        if (o == 15) {
            return "CALL_FORWARDING_UNCONDITIONAL";
        }
        if (o == 16) {
            return "RESERVED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("CALL_FORWARDING_BUSY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CALL_FORWARDING_NO_REPLY");
            flipped |= 2;
        }
        if ((o & 9) == 9) {
            list.add("CALLED_DTE_OUT_OF_ORDER");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("CALL_FORWARDING_BY_THE_CALLED_DTE");
            flipped |= 10;
        }
        if ((o & 15) == 15) {
            list.add("CALL_FORWARDING_UNCONDITIONAL");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("RESERVED");
            flipped |= 16;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
