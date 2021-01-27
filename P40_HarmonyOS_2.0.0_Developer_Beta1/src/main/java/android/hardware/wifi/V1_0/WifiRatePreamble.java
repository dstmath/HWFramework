package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiRatePreamble {
    public static final int CCK = 1;
    public static final int HT = 2;
    public static final int OFDM = 0;
    public static final int RESERVED = 4;
    public static final int VHT = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "OFDM";
        }
        if (o == 1) {
            return "CCK";
        }
        if (o == 2) {
            return "HT";
        }
        if (o == 3) {
            return "VHT";
        }
        if (o == 4) {
            return "RESERVED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OFDM");
        if ((o & 1) == 1) {
            list.add("CCK");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("HT");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("VHT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("RESERVED");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
