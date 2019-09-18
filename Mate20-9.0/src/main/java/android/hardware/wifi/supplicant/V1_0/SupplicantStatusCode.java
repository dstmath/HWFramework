package android.hardware.wifi.supplicant.V1_0;

import java.util.ArrayList;

public final class SupplicantStatusCode {
    public static final int FAILURE_ARGS_INVALID = 2;
    public static final int FAILURE_IFACE_DISABLED = 6;
    public static final int FAILURE_IFACE_EXISTS = 5;
    public static final int FAILURE_IFACE_INVALID = 3;
    public static final int FAILURE_IFACE_NOT_DISCONNECTED = 7;
    public static final int FAILURE_IFACE_UNKNOWN = 4;
    public static final int FAILURE_NETWORK_INVALID = 8;
    public static final int FAILURE_NETWORK_UNKNOWN = 9;
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
            return "FAILURE_IFACE_INVALID";
        }
        if (o == 4) {
            return "FAILURE_IFACE_UNKNOWN";
        }
        if (o == 5) {
            return "FAILURE_IFACE_EXISTS";
        }
        if (o == 6) {
            return "FAILURE_IFACE_DISABLED";
        }
        if (o == 7) {
            return "FAILURE_IFACE_NOT_DISCONNECTED";
        }
        if (o == 8) {
            return "FAILURE_NETWORK_INVALID";
        }
        if (o == 9) {
            return "FAILURE_NETWORK_UNKNOWN";
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
            list.add("FAILURE_IFACE_INVALID");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("FAILURE_IFACE_UNKNOWN");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("FAILURE_IFACE_EXISTS");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("FAILURE_IFACE_DISABLED");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("FAILURE_IFACE_NOT_DISCONNECTED");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("FAILURE_NETWORK_INVALID");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("FAILURE_NETWORK_UNKNOWN");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
