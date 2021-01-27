package android.hardware.health.V1_0;

import java.util.ArrayList;

public final class BatteryHealth {
    public static final int COLD = 7;
    public static final int DEAD = 4;
    public static final int GOOD = 2;
    public static final int OVERHEAT = 3;
    public static final int OVER_VOLTAGE = 5;
    public static final int UNKNOWN = 1;
    public static final int UNSPECIFIED_FAILURE = 6;

    public static final String toString(int o) {
        if (o == 1) {
            return "UNKNOWN";
        }
        if (o == 2) {
            return "GOOD";
        }
        if (o == 3) {
            return "OVERHEAT";
        }
        if (o == 4) {
            return "DEAD";
        }
        if (o == 5) {
            return "OVER_VOLTAGE";
        }
        if (o == 6) {
            return "UNSPECIFIED_FAILURE";
        }
        if (o == 7) {
            return "COLD";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("UNKNOWN");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("GOOD");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("OVERHEAT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("DEAD");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("OVER_VOLTAGE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("UNSPECIFIED_FAILURE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("COLD");
            flipped |= 7;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
