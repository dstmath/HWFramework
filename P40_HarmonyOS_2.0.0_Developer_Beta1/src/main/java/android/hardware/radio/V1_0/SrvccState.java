package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SrvccState {
    public static final int HANDOVER_CANCELED = 3;
    public static final int HANDOVER_COMPLETED = 1;
    public static final int HANDOVER_FAILED = 2;
    public static final int HANDOVER_STARTED = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "HANDOVER_STARTED";
        }
        if (o == 1) {
            return "HANDOVER_COMPLETED";
        }
        if (o == 2) {
            return "HANDOVER_FAILED";
        }
        if (o == 3) {
            return "HANDOVER_CANCELED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("HANDOVER_STARTED");
        if ((o & 1) == 1) {
            list.add("HANDOVER_COMPLETED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("HANDOVER_FAILED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("HANDOVER_CANCELED");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
