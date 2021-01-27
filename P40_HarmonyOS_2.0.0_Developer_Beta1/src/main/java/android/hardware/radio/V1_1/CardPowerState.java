package android.hardware.radio.V1_1;

import java.util.ArrayList;

public final class CardPowerState {
    public static final int POWER_DOWN = 0;
    public static final int POWER_UP = 1;
    public static final int POWER_UP_PASS_THROUGH = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "POWER_DOWN";
        }
        if (o == 1) {
            return "POWER_UP";
        }
        if (o == 2) {
            return "POWER_UP_PASS_THROUGH";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("POWER_DOWN");
        if ((o & 1) == 1) {
            list.add("POWER_UP");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("POWER_UP_PASS_THROUGH");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
