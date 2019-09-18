package android.hardware.biometrics.fingerprint.V2_1;

import java.util.ArrayList;

public final class FingerprintError {
    public static final int ERROR_CANCELED = 5;
    public static final int ERROR_HW_UNAVAILABLE = 1;
    public static final int ERROR_LOCKOUT = 7;
    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_NO_SPACE = 4;
    public static final int ERROR_TIMEOUT = 3;
    public static final int ERROR_UNABLE_TO_PROCESS = 2;
    public static final int ERROR_UNABLE_TO_REMOVE = 6;
    public static final int ERROR_VENDOR = 8;

    public static final String toString(int o) {
        if (o == 0) {
            return "ERROR_NO_ERROR";
        }
        if (o == 1) {
            return "ERROR_HW_UNAVAILABLE";
        }
        if (o == 2) {
            return "ERROR_UNABLE_TO_PROCESS";
        }
        if (o == 3) {
            return "ERROR_TIMEOUT";
        }
        if (o == 4) {
            return "ERROR_NO_SPACE";
        }
        if (o == 5) {
            return "ERROR_CANCELED";
        }
        if (o == 6) {
            return "ERROR_UNABLE_TO_REMOVE";
        }
        if (o == 7) {
            return "ERROR_LOCKOUT";
        }
        if (o == 8) {
            return "ERROR_VENDOR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("ERROR_NO_ERROR");
        if ((o & 1) == 1) {
            list.add("ERROR_HW_UNAVAILABLE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ERROR_UNABLE_TO_PROCESS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("ERROR_TIMEOUT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ERROR_NO_SPACE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ERROR_CANCELED");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("ERROR_UNABLE_TO_REMOVE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("ERROR_LOCKOUT");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("ERROR_VENDOR");
            flipped |= 8;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
