package android.hardware.wifi.supplicant.V1_2;

import java.util.ArrayList;

public final class DppProgressCode {
    public static final int AUTHENTICATION_SUCCESS = 0;
    public static final int RESPONSE_PENDING = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "AUTHENTICATION_SUCCESS";
        }
        if (o == 1) {
            return "RESPONSE_PENDING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("AUTHENTICATION_SUCCESS");
        if ((o & 1) == 1) {
            list.add("RESPONSE_PENDING");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
