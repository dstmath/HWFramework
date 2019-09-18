package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class DeviceStateType {
    public static final int CHARGING_STATE = 1;
    public static final int LOW_DATA_EXPECTED = 2;
    public static final int POWER_SAVE_MODE = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "POWER_SAVE_MODE";
        }
        if (o == 1) {
            return "CHARGING_STATE";
        }
        if (o == 2) {
            return "LOW_DATA_EXPECTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("POWER_SAVE_MODE");
        if ((o & 1) == 1) {
            list.add("CHARGING_STATE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("LOW_DATA_EXPECTED");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
