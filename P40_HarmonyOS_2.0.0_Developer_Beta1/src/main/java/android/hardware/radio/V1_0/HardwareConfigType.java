package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class HardwareConfigType {
    public static final int MODEM = 0;
    public static final int SIM = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "MODEM";
        }
        if (o == 1) {
            return "SIM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("MODEM");
        if ((o & 1) == 1) {
            list.add("SIM");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
