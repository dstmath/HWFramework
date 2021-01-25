package vendor.huawei.hardware.wifi.supplicant.V3_0;

import java.util.ArrayList;

public final class DppFailureCode {
    public static final int AUTHENTICATION = 1;
    public static final int BUSY = 4;
    public static final int CONFIGURATION = 3;
    public static final int FAILURE = 6;
    public static final int INVALID_URI = 0;
    public static final int NOT_COMPATIBLE = 2;
    public static final int NOT_SUPPORTED = 7;
    public static final int TIMEOUT = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "INVALID_URI";
        }
        if (o == 1) {
            return "AUTHENTICATION";
        }
        if (o == 2) {
            return "NOT_COMPATIBLE";
        }
        if (o == 3) {
            return "CONFIGURATION";
        }
        if (o == 4) {
            return "BUSY";
        }
        if (o == 5) {
            return "TIMEOUT";
        }
        if (o == 6) {
            return "FAILURE";
        }
        if (o == 7) {
            return "NOT_SUPPORTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("INVALID_URI");
        if ((o & 1) == 1) {
            list.add("AUTHENTICATION");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NOT_COMPATIBLE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CONFIGURATION");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("BUSY");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TIMEOUT");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("FAILURE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("NOT_SUPPORTED");
            flipped |= 7;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
