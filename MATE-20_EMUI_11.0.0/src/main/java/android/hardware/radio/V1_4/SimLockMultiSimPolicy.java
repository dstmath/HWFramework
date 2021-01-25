package android.hardware.radio.V1_4;

import java.util.ArrayList;

public final class SimLockMultiSimPolicy {
    public static final int NO_MULTISIM_POLICY = 0;
    public static final int ONE_VALID_SIM_MUST_BE_PRESENT = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NO_MULTISIM_POLICY";
        }
        if (o == 1) {
            return "ONE_VALID_SIM_MUST_BE_PRESENT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NO_MULTISIM_POLICY");
        if ((o & 1) == 1) {
            list.add("ONE_VALID_SIM_MUST_BE_PRESENT");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
