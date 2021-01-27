package android.hardware.thermal.V1_0;

import java.util.ArrayList;

public final class CoolingType {
    public static final int FAN_RPM = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "FAN_RPM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        list.add("FAN_RPM");
        if (o != 0) {
            list.add("0x" + Integer.toHexString((~0) & o));
        }
        return String.join(" | ", list);
    }
}
