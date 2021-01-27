package android.hardware.tetheroffload.control.V1_0;

import java.util.ArrayList;

public final class OffloadCallbackEvent {
    public static final int OFFLOAD_STARTED = 1;
    public static final int OFFLOAD_STOPPED_ERROR = 2;
    public static final int OFFLOAD_STOPPED_LIMIT_REACHED = 5;
    public static final int OFFLOAD_STOPPED_UNSUPPORTED = 3;
    public static final int OFFLOAD_SUPPORT_AVAILABLE = 4;

    public static final String toString(int o) {
        if (o == 1) {
            return "OFFLOAD_STARTED";
        }
        if (o == 2) {
            return "OFFLOAD_STOPPED_ERROR";
        }
        if (o == 3) {
            return "OFFLOAD_STOPPED_UNSUPPORTED";
        }
        if (o == 4) {
            return "OFFLOAD_SUPPORT_AVAILABLE";
        }
        if (o == 5) {
            return "OFFLOAD_STOPPED_LIMIT_REACHED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("OFFLOAD_STARTED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("OFFLOAD_STOPPED_ERROR");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("OFFLOAD_STOPPED_UNSUPPORTED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("OFFLOAD_SUPPORT_AVAILABLE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("OFFLOAD_STOPPED_LIMIT_REACHED");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
