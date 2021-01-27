package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiStatusCode {
    public static final int ERROR_BUSY = 8;
    public static final int ERROR_INVALID_ARGS = 7;
    public static final int ERROR_NOT_AVAILABLE = 5;
    public static final int ERROR_NOT_STARTED = 6;
    public static final int ERROR_NOT_SUPPORTED = 4;
    public static final int ERROR_UNKNOWN = 9;
    public static final int ERROR_WIFI_CHIP_INVALID = 1;
    public static final int ERROR_WIFI_IFACE_INVALID = 2;
    public static final int ERROR_WIFI_RTT_CONTROLLER_INVALID = 3;
    public static final int SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "ERROR_WIFI_CHIP_INVALID";
        }
        if (o == 2) {
            return "ERROR_WIFI_IFACE_INVALID";
        }
        if (o == 3) {
            return "ERROR_WIFI_RTT_CONTROLLER_INVALID";
        }
        if (o == 4) {
            return "ERROR_NOT_SUPPORTED";
        }
        if (o == 5) {
            return "ERROR_NOT_AVAILABLE";
        }
        if (o == 6) {
            return "ERROR_NOT_STARTED";
        }
        if (o == 7) {
            return "ERROR_INVALID_ARGS";
        }
        if (o == 8) {
            return "ERROR_BUSY";
        }
        if (o == 9) {
            return "ERROR_UNKNOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("ERROR_WIFI_CHIP_INVALID");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ERROR_WIFI_IFACE_INVALID");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("ERROR_WIFI_RTT_CONTROLLER_INVALID");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ERROR_NOT_SUPPORTED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ERROR_NOT_AVAILABLE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("ERROR_NOT_STARTED");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("ERROR_INVALID_ARGS");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("ERROR_BUSY");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("ERROR_UNKNOWN");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
