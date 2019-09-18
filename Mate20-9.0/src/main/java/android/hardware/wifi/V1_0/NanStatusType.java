package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanStatusType {
    public static final int ALREADY_ENABLED = 10;
    public static final int FOLLOWUP_TX_QUEUE_FULL = 11;
    public static final int INTERNAL_FAILURE = 1;
    public static final int INVALID_ARGS = 5;
    public static final int INVALID_NDP_ID = 7;
    public static final int INVALID_PEER_ID = 6;
    public static final int INVALID_SESSION_ID = 3;
    public static final int NAN_NOT_ALLOWED = 8;
    public static final int NO_OTA_ACK = 9;
    public static final int NO_RESOURCES_AVAILABLE = 4;
    public static final int PROTOCOL_FAILURE = 2;
    public static final int SUCCESS = 0;
    public static final int UNSUPPORTED_CONCURRENCY_NAN_DISABLED = 12;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "INTERNAL_FAILURE";
        }
        if (o == 2) {
            return "PROTOCOL_FAILURE";
        }
        if (o == 3) {
            return "INVALID_SESSION_ID";
        }
        if (o == 4) {
            return "NO_RESOURCES_AVAILABLE";
        }
        if (o == 5) {
            return "INVALID_ARGS";
        }
        if (o == 6) {
            return "INVALID_PEER_ID";
        }
        if (o == 7) {
            return "INVALID_NDP_ID";
        }
        if (o == 8) {
            return "NAN_NOT_ALLOWED";
        }
        if (o == 9) {
            return "NO_OTA_ACK";
        }
        if (o == 10) {
            return "ALREADY_ENABLED";
        }
        if (o == 11) {
            return "FOLLOWUP_TX_QUEUE_FULL";
        }
        if (o == 12) {
            return "UNSUPPORTED_CONCURRENCY_NAN_DISABLED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("INTERNAL_FAILURE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("PROTOCOL_FAILURE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("INVALID_SESSION_ID");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("NO_RESOURCES_AVAILABLE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("INVALID_ARGS");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("INVALID_PEER_ID");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("INVALID_NDP_ID");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("NAN_NOT_ALLOWED");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("NO_OTA_ACK");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("ALREADY_ENABLED");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("FOLLOWUP_TX_QUEUE_FULL");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("UNSUPPORTED_CONCURRENCY_NAN_DISABLED");
            flipped |= 12;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
