package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Event {
    public static final int FM_READY = 0;
    public static final int RDS_AVAL = 5;
    public static final int SEARCH_CANCELLED = 4;
    public static final int SEARCH_COMPLETE = 3;
    public static final int SEARCH_IN_PROGRESS = 2;
    public static final int TUNE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "FM_READY";
        }
        if (o == 1) {
            return "TUNE";
        }
        if (o == 2) {
            return "SEARCH_IN_PROGRESS";
        }
        if (o == 3) {
            return "SEARCH_COMPLETE";
        }
        if (o == 4) {
            return "SEARCH_CANCELLED";
        }
        if (o == 5) {
            return "RDS_AVAL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("FM_READY");
        if ((o & 1) == 1) {
            list.add("TUNE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SEARCH_IN_PROGRESS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SEARCH_COMPLETE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SEARCH_CANCELLED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("RDS_AVAL");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
