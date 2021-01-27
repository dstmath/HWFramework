package android.hardware.biometrics.face.V1_0;

import java.util.ArrayList;

public final class FaceError {
    public static final int CANCELED = 5;
    public static final int HW_UNAVAILABLE = 1;
    public static final int LOCKOUT = 7;
    public static final int LOCKOUT_PERMANENT = 9;
    public static final int NO_SPACE = 4;
    public static final int TIMEOUT = 3;
    public static final int UNABLE_TO_PROCESS = 2;
    public static final int UNABLE_TO_REMOVE = 6;
    public static final int VENDOR = 8;

    public static final String toString(int o) {
        if (o == 1) {
            return "HW_UNAVAILABLE";
        }
        if (o == 2) {
            return "UNABLE_TO_PROCESS";
        }
        if (o == 3) {
            return "TIMEOUT";
        }
        if (o == 4) {
            return "NO_SPACE";
        }
        if (o == 5) {
            return "CANCELED";
        }
        if (o == 6) {
            return "UNABLE_TO_REMOVE";
        }
        if (o == 7) {
            return "LOCKOUT";
        }
        if (o == 8) {
            return "VENDOR";
        }
        if (o == 9) {
            return "LOCKOUT_PERMANENT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("HW_UNAVAILABLE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("UNABLE_TO_PROCESS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TIMEOUT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("NO_SPACE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("CANCELED");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("UNABLE_TO_REMOVE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("LOCKOUT");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("VENDOR");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("LOCKOUT_PERMANENT");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
