package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SapResultCode {
    public static final int CARD_ALREADY_POWERED_OFF = 3;
    public static final int CARD_ALREADY_POWERED_ON = 5;
    public static final int CARD_NOT_ACCESSSIBLE = 2;
    public static final int CARD_REMOVED = 4;
    public static final int DATA_NOT_AVAILABLE = 6;
    public static final int GENERIC_FAILURE = 1;
    public static final int NOT_SUPPORTED = 7;
    public static final int SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "GENERIC_FAILURE";
        }
        if (o == 2) {
            return "CARD_NOT_ACCESSSIBLE";
        }
        if (o == 3) {
            return "CARD_ALREADY_POWERED_OFF";
        }
        if (o == 4) {
            return "CARD_REMOVED";
        }
        if (o == 5) {
            return "CARD_ALREADY_POWERED_ON";
        }
        if (o == 6) {
            return "DATA_NOT_AVAILABLE";
        }
        if (o == 7) {
            return "NOT_SUPPORTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("GENERIC_FAILURE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CARD_NOT_ACCESSSIBLE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CARD_ALREADY_POWERED_OFF");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CARD_REMOVED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("CARD_ALREADY_POWERED_ON");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("DATA_NOT_AVAILABLE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("NOT_SUPPORTED");
            flipped |= 7;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
