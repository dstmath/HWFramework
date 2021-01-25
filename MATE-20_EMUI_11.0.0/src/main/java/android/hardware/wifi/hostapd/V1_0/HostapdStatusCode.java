package android.hardware.wifi.hostapd.V1_0;

import java.util.ArrayList;

public final class HostapdStatusCode {
    public static final int FAILURE_ARGS_INVALID = 2;
    public static final int FAILURE_IFACE_EXISTS = 4;
    public static final int FAILURE_IFACE_UNKNOWN = 3;
    public static final int FAILURE_UNKNOWN = 1;
    public static final int SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "FAILURE_UNKNOWN";
        }
        if (o == 2) {
            return "FAILURE_ARGS_INVALID";
        }
        if (o == 3) {
            return "FAILURE_IFACE_UNKNOWN";
        }
        if (o == 4) {
            return "FAILURE_IFACE_EXISTS";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("FAILURE_UNKNOWN");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FAILURE_ARGS_INVALID");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("FAILURE_IFACE_UNKNOWN");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("FAILURE_IFACE_EXISTS");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
