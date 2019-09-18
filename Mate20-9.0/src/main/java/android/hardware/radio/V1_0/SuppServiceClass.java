package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SuppServiceClass {
    public static final int DATA = 2;
    public static final int DATA_ASYNC = 32;
    public static final int DATA_SYNC = 16;
    public static final int FAX = 4;
    public static final int MAX = 128;
    public static final int NONE = 0;
    public static final int PACKET = 64;
    public static final int PAD = 128;
    public static final int SMS = 8;
    public static final int VOICE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "VOICE";
        }
        if (o == 2) {
            return "DATA";
        }
        if (o == 4) {
            return "FAX";
        }
        if (o == 8) {
            return "SMS";
        }
        if (o == 16) {
            return "DATA_SYNC";
        }
        if (o == 32) {
            return "DATA_ASYNC";
        }
        if (o == 64) {
            return "PACKET";
        }
        if (o == 128) {
            return "PAD";
        }
        if (o == 128) {
            return "MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("VOICE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("DATA");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("FAX");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("SMS");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("DATA_SYNC");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("DATA_ASYNC");
            flipped |= 32;
        }
        if ((o & 64) == 64) {
            list.add("PACKET");
            flipped |= 64;
        }
        if ((o & 128) == 128) {
            list.add("PAD");
            flipped |= 128;
        }
        if ((o & 128) == 128) {
            list.add("MAX");
            flipped |= 128;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
