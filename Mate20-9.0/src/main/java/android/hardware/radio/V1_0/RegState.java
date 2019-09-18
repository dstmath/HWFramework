package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RegState {
    public static final int NOT_REG_MT_NOT_SEARCHING_OP = 0;
    public static final int NOT_REG_MT_NOT_SEARCHING_OP_EM = 10;
    public static final int NOT_REG_MT_SEARCHING_OP = 2;
    public static final int NOT_REG_MT_SEARCHING_OP_EM = 12;
    public static final int REG_DENIED = 3;
    public static final int REG_DENIED_EM = 13;
    public static final int REG_HOME = 1;
    public static final int REG_ROAMING = 5;
    public static final int UNKNOWN = 4;
    public static final int UNKNOWN_EM = 14;

    public static final String toString(int o) {
        if (o == 0) {
            return "NOT_REG_MT_NOT_SEARCHING_OP";
        }
        if (o == 1) {
            return "REG_HOME";
        }
        if (o == 2) {
            return "NOT_REG_MT_SEARCHING_OP";
        }
        if (o == 3) {
            return "REG_DENIED";
        }
        if (o == 4) {
            return "UNKNOWN";
        }
        if (o == 5) {
            return "REG_ROAMING";
        }
        if (o == 10) {
            return "NOT_REG_MT_NOT_SEARCHING_OP_EM";
        }
        if (o == 12) {
            return "NOT_REG_MT_SEARCHING_OP_EM";
        }
        if (o == 13) {
            return "REG_DENIED_EM";
        }
        if (o == 14) {
            return "UNKNOWN_EM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NOT_REG_MT_NOT_SEARCHING_OP");
        if ((o & 1) == 1) {
            list.add("REG_HOME");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NOT_REG_MT_SEARCHING_OP");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("REG_DENIED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("UNKNOWN");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("REG_ROAMING");
            flipped |= 5;
        }
        if ((o & 10) == 10) {
            list.add("NOT_REG_MT_NOT_SEARCHING_OP_EM");
            flipped |= 10;
        }
        if ((o & 12) == 12) {
            list.add("NOT_REG_MT_SEARCHING_OP_EM");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("REG_DENIED_EM");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("UNKNOWN_EM");
            flipped |= 14;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
