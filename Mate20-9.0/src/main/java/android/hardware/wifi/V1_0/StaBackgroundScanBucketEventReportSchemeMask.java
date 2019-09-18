package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class StaBackgroundScanBucketEventReportSchemeMask {
    public static final int EACH_SCAN = 1;
    public static final int FULL_RESULTS = 2;
    public static final int NO_BATCH = 4;

    public static final String toString(int o) {
        if (o == 1) {
            return "EACH_SCAN";
        }
        if (o == 2) {
            return "FULL_RESULTS";
        }
        if (o == 4) {
            return "NO_BATCH";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("EACH_SCAN");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FULL_RESULTS");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("NO_BATCH");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
