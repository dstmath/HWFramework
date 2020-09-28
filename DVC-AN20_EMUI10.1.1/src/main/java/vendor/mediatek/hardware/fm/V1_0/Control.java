package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Control {
    public static final int EMPHASIS = 5;
    public static final int LP_MODE = 8;
    public static final int RDS_STD = 6;
    public static final int REGION = 2;
    public static final int SCAN_DEWELL = 1;
    public static final int SEARCH_MODE = 0;
    public static final int SEARCH_PI = 4;
    public static final int SEARCH_PTY = 3;
    public static final int SPACING = 7;

    public static final String toString(int o) {
        if (o == 0) {
            return "SEARCH_MODE";
        }
        if (o == 1) {
            return "SCAN_DEWELL";
        }
        if (o == 2) {
            return "REGION";
        }
        if (o == 3) {
            return "SEARCH_PTY";
        }
        if (o == 4) {
            return "SEARCH_PI";
        }
        if (o == 5) {
            return "EMPHASIS";
        }
        if (o == 6) {
            return "RDS_STD";
        }
        if (o == 7) {
            return "SPACING";
        }
        if (o == 8) {
            return "LP_MODE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SEARCH_MODE");
        if ((o & 1) == 1) {
            list.add("SCAN_DEWELL");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("REGION");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SEARCH_PTY");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SEARCH_PI");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("EMPHASIS");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("RDS_STD");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("SPACING");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("LP_MODE");
            flipped |= 8;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
