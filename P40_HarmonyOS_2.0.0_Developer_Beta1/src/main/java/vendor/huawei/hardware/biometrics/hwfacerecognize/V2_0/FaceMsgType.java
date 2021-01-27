package vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0;

import java.util.ArrayList;

public final class FaceMsgType {
    public static final int AUTHENTICATE_ACQUIRED = 4;
    public static final int AUTHENTICATE_RESULT = 3;
    public static final int CANCEL = 5;
    public static final int ENROLL_ACQUIRED = 2;
    public static final int ENROLL_RESULT = 1;
    public static final int INIT_RESULT = 7;
    public static final int RELEASE_RESULT = 8;
    public static final int REMOVE_RESULT = 6;

    public static final String toString(int o) {
        if (o == 1) {
            return "ENROLL_RESULT";
        }
        if (o == 2) {
            return "ENROLL_ACQUIRED";
        }
        if (o == 3) {
            return "AUTHENTICATE_RESULT";
        }
        if (o == 4) {
            return "AUTHENTICATE_ACQUIRED";
        }
        if (o == 5) {
            return "CANCEL";
        }
        if (o == 6) {
            return "REMOVE_RESULT";
        }
        if (o == 7) {
            return "INIT_RESULT";
        }
        if (o == 8) {
            return "RELEASE_RESULT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("ENROLL_RESULT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ENROLL_ACQUIRED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("AUTHENTICATE_RESULT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("AUTHENTICATE_ACQUIRED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("CANCEL");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("REMOVE_RESULT");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("INIT_RESULT");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("RELEASE_RESULT");
            flipped |= 8;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
