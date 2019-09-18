package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioCapabilityPhase {
    public static final int APPLY = 2;
    public static final int CONFIGURED = 0;
    public static final int FINISH = 4;
    public static final int START = 1;
    public static final int UNSOL_RSP = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "CONFIGURED";
        }
        if (o == 1) {
            return "START";
        }
        if (o == 2) {
            return "APPLY";
        }
        if (o == 3) {
            return "UNSOL_RSP";
        }
        if (o == 4) {
            return "FINISH";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("CONFIGURED");
        if ((o & 1) == 1) {
            list.add("START");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("APPLY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("UNSOL_RSP");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("FINISH");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
