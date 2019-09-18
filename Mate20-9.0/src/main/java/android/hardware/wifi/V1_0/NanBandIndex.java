package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanBandIndex {
    public static final int NAN_BAND_24GHZ = 0;
    public static final int NAN_BAND_5GHZ = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NAN_BAND_24GHZ";
        }
        if (o == 1) {
            return "NAN_BAND_5GHZ";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NAN_BAND_24GHZ");
        if ((o & 1) == 1) {
            list.add("NAN_BAND_5GHZ");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
