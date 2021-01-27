package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioConst {
    public static final int CARD_MAX_APPS = 8;
    public static final int CDMA_ALPHA_INFO_BUFFER_LENGTH = 64;
    public static final int CDMA_MAX_NUMBER_OF_INFO_RECS = 10;
    public static final int CDMA_NUMBER_INFO_BUFFER_LENGTH = 81;
    public static final int MAX_CLIENT_ID_LENGTH = 2;
    public static final int MAX_DEBUG_SOCKET_NAME_LENGTH = 12;
    public static final int MAX_QEMU_PIPE_NAME_LENGTH = 11;
    public static final int MAX_RILDS = 3;
    public static final int MAX_SOCKET_NAME_LENGTH = 6;
    public static final int MAX_UUID_LENGTH = 64;
    public static final int NUM_SERVICE_CLASSES = 7;
    public static final int NUM_TX_POWER_LEVELS = 5;
    public static final int SS_INFO_MAX = 4;

    public static final String toString(int o) {
        if (o == 64) {
            return "CDMA_ALPHA_INFO_BUFFER_LENGTH";
        }
        if (o == 81) {
            return "CDMA_NUMBER_INFO_BUFFER_LENGTH";
        }
        if (o == 3) {
            return "MAX_RILDS";
        }
        if (o == 6) {
            return "MAX_SOCKET_NAME_LENGTH";
        }
        if (o == 2) {
            return "MAX_CLIENT_ID_LENGTH";
        }
        if (o == 12) {
            return "MAX_DEBUG_SOCKET_NAME_LENGTH";
        }
        if (o == 11) {
            return "MAX_QEMU_PIPE_NAME_LENGTH";
        }
        if (o == 64) {
            return "MAX_UUID_LENGTH";
        }
        if (o == 8) {
            return "CARD_MAX_APPS";
        }
        if (o == 10) {
            return "CDMA_MAX_NUMBER_OF_INFO_RECS";
        }
        if (o == 4) {
            return "SS_INFO_MAX";
        }
        if (o == 7) {
            return "NUM_SERVICE_CLASSES";
        }
        if (o == 5) {
            return "NUM_TX_POWER_LEVELS";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 64) == 64) {
            list.add("CDMA_ALPHA_INFO_BUFFER_LENGTH");
            flipped = 0 | 64;
        }
        if ((o & 81) == 81) {
            list.add("CDMA_NUMBER_INFO_BUFFER_LENGTH");
            flipped |= 81;
        }
        if ((o & 3) == 3) {
            list.add("MAX_RILDS");
            flipped |= 3;
        }
        if ((o & 6) == 6) {
            list.add("MAX_SOCKET_NAME_LENGTH");
            flipped |= 6;
        }
        if ((o & 2) == 2) {
            list.add("MAX_CLIENT_ID_LENGTH");
            flipped |= 2;
        }
        if ((o & 12) == 12) {
            list.add("MAX_DEBUG_SOCKET_NAME_LENGTH");
            flipped |= 12;
        }
        if ((o & 11) == 11) {
            list.add("MAX_QEMU_PIPE_NAME_LENGTH");
            flipped |= 11;
        }
        if ((o & 64) == 64) {
            list.add("MAX_UUID_LENGTH");
            flipped |= 64;
        }
        if ((o & 8) == 8) {
            list.add("CARD_MAX_APPS");
            flipped |= 8;
        }
        if ((o & 10) == 10) {
            list.add("CDMA_MAX_NUMBER_OF_INFO_RECS");
            flipped |= 10;
        }
        if ((o & 4) == 4) {
            list.add("SS_INFO_MAX");
            flipped |= 4;
        }
        if ((o & 7) == 7) {
            list.add("NUM_SERVICE_CLASSES");
            flipped |= 7;
        }
        if ((o & 5) == 5) {
            list.add("NUM_TX_POWER_LEVELS");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
