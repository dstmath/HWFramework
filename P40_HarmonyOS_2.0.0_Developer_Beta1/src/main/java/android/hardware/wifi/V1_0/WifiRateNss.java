package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiRateNss {
    public static final int NSS_1x1 = 0;
    public static final int NSS_2x2 = 1;
    public static final int NSS_3x3 = 2;
    public static final int NSS_4x4 = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "NSS_1x1";
        }
        if (o == 1) {
            return "NSS_2x2";
        }
        if (o == 2) {
            return "NSS_3x3";
        }
        if (o == 3) {
            return "NSS_4x4";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NSS_1x1");
        if ((o & 1) == 1) {
            list.add("NSS_2x2");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NSS_3x3");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("NSS_4x4");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
