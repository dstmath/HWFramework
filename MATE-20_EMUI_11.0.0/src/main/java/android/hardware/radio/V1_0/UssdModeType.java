package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class UssdModeType {
    public static final int LOCAL_CLIENT = 3;
    public static final int NOTIFY = 0;
    public static final int NOT_SUPPORTED = 4;
    public static final int NW_RELEASE = 2;
    public static final int NW_TIMEOUT = 5;
    public static final int REQUEST = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NOTIFY";
        }
        if (o == 1) {
            return "REQUEST";
        }
        if (o == 2) {
            return "NW_RELEASE";
        }
        if (o == 3) {
            return "LOCAL_CLIENT";
        }
        if (o == 4) {
            return "NOT_SUPPORTED";
        }
        if (o == 5) {
            return "NW_TIMEOUT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NOTIFY");
        if ((o & 1) == 1) {
            list.add("REQUEST");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NW_RELEASE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("LOCAL_CLIENT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("NOT_SUPPORTED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("NW_TIMEOUT");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
