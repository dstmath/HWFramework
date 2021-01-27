package vendor.huawei.hardware.radio_radar.V1_0;

import java.util.ArrayList;

public final class RadioError {
    public static final int GENERIC_FAILURE = 2;
    public static final int INVALID_RESPONSE = 66;
    public static final int NONE = 0;
    public static final int RADIO_NOT_AVAILABLE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "RADIO_NOT_AVAILABLE";
        }
        if (o == 2) {
            return "GENERIC_FAILURE";
        }
        if (o == 66) {
            return "INVALID_RESPONSE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("RADIO_NOT_AVAILABLE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("GENERIC_FAILURE");
            flipped |= 2;
        }
        if ((o & 66) == 66) {
            list.add("INVALID_RESPONSE");
            flipped |= 66;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
