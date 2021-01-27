package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class SegmentationMode {
    public static final int HIGH_BIT_AL_MODE = 1;
    public static final int HIGH_BIT_COMP_MODE = 4;
    public static final int HIGH_BIT_DETAIL_MODE = 3;
    public static final int HIGH_BIT_WCG_MODE = 2;
    public static final int LOW_BIT_MODE = 0;
    public static final int SEG_MODE_MAX = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "LOW_BIT_MODE";
        }
        if (o == 1) {
            return "HIGH_BIT_AL_MODE";
        }
        if (o == 2) {
            return "HIGH_BIT_WCG_MODE";
        }
        if (o == 3) {
            return "HIGH_BIT_DETAIL_MODE";
        }
        if (o == 4) {
            return "HIGH_BIT_COMP_MODE";
        }
        if (o == 5) {
            return "SEG_MODE_MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("LOW_BIT_MODE");
        if ((o & 1) == 1) {
            list.add("HIGH_BIT_AL_MODE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("HIGH_BIT_WCG_MODE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("HIGH_BIT_DETAIL_MODE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("HIGH_BIT_COMP_MODE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("SEG_MODE_MAX");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
