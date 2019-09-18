package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioIndicationType {
    public static final int UNSOLICITED = 0;
    public static final int UNSOLICITED_ACK_EXP = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNSOLICITED";
        }
        if (o == 1) {
            return "UNSOLICITED_ACK_EXP";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNSOLICITED");
        if ((o & 1) == 1) {
            list.add("UNSOLICITED_ACK_EXP");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
