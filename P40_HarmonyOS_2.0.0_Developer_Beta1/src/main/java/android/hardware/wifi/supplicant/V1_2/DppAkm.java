package android.hardware.wifi.supplicant.V1_2;

import java.util.ArrayList;

public final class DppAkm {
    public static final int DPP = 3;
    public static final int PSK = 0;
    public static final int PSK_SAE = 1;
    public static final int SAE = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "PSK";
        }
        if (o == 1) {
            return "PSK_SAE";
        }
        if (o == 2) {
            return "SAE";
        }
        if (o == 3) {
            return "DPP";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("PSK");
        if ((o & 1) == 1) {
            list.add("PSK_SAE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SAE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("DPP");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
