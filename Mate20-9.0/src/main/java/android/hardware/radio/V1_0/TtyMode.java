package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class TtyMode {
    public static final int FULL = 1;
    public static final int HCO = 2;
    public static final int OFF = 0;
    public static final int VCO = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "OFF";
        }
        if (o == 1) {
            return "FULL";
        }
        if (o == 2) {
            return "HCO";
        }
        if (o == 3) {
            return "VCO";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OFF");
        if ((o & 1) == 1) {
            list.add("FULL");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("HCO");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("VCO");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
