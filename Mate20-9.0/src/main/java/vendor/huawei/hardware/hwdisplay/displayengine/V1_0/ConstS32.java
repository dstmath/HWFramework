package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class ConstS32 {
    public static final int HIGH_BITS_AL_MARK = 3840;
    public static final int HIGH_BITS_COMP_MARK = 2146435072;
    public static final int HIGH_BITS_DETAIL_MARK = 983040;
    public static final int HIGH_BITS_WCG_MARK = 61440;
    public static final int LOW_BITS_MARK = 255;

    public static final String toString(int o) {
        if (o == 255) {
            return "LOW_BITS_MARK";
        }
        if (o == 3840) {
            return "HIGH_BITS_AL_MARK";
        }
        if (o == 61440) {
            return "HIGH_BITS_WCG_MARK";
        }
        if (o == 983040) {
            return "HIGH_BITS_DETAIL_MARK";
        }
        if (o == 2146435072) {
            return "HIGH_BITS_COMP_MARK";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 255) == 255) {
            list.add("LOW_BITS_MARK");
            flipped = 0 | 255;
        }
        if ((o & HIGH_BITS_AL_MARK) == 3840) {
            list.add("HIGH_BITS_AL_MARK");
            flipped |= HIGH_BITS_AL_MARK;
        }
        if ((o & HIGH_BITS_WCG_MARK) == 61440) {
            list.add("HIGH_BITS_WCG_MARK");
            flipped |= HIGH_BITS_WCG_MARK;
        }
        if ((o & 983040) == 983040) {
            list.add("HIGH_BITS_DETAIL_MARK");
            flipped |= 983040;
        }
        if ((o & HIGH_BITS_COMP_MARK) == 2146435072) {
            list.add("HIGH_BITS_COMP_MARK");
            flipped |= HIGH_BITS_COMP_MARK;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
