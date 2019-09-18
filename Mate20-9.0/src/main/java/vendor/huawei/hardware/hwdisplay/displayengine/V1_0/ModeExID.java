package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class ModeExID {
    public static final int MODE_AL = 35;
    public static final int MODE_DATA = 36;
    public static final int MODE_DATA_HDR10 = 37;
    public static final int MODE_DATA_XCC = 38;
    public static final int MODE_DIMMING = 40;
    public static final int MODE_EX_MAX = 42;
    public static final int MODE_HBM = 41;
    public static final int MODE_XNIT_CHANGE = 39;

    public static final String toString(int o) {
        if (o == 35) {
            return "MODE_AL";
        }
        if (o == 36) {
            return "MODE_DATA";
        }
        if (o == 37) {
            return "MODE_DATA_HDR10";
        }
        if (o == 38) {
            return "MODE_DATA_XCC";
        }
        if (o == 39) {
            return "MODE_XNIT_CHANGE";
        }
        if (o == 40) {
            return "MODE_DIMMING";
        }
        if (o == 41) {
            return "MODE_HBM";
        }
        if (o == 42) {
            return "MODE_EX_MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 35) == 35) {
            list.add("MODE_AL");
            flipped = 0 | 35;
        }
        if ((o & 36) == 36) {
            list.add("MODE_DATA");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("MODE_DATA_HDR10");
            flipped |= 37;
        }
        if ((o & 38) == 38) {
            list.add("MODE_DATA_XCC");
            flipped |= 38;
        }
        if ((o & 39) == 39) {
            list.add("MODE_XNIT_CHANGE");
            flipped |= 39;
        }
        if ((o & 40) == 40) {
            list.add("MODE_DIMMING");
            flipped |= 40;
        }
        if ((o & 41) == 41) {
            list.add("MODE_HBM");
            flipped |= 41;
        }
        if ((o & 42) == 42) {
            list.add("MODE_EX_MAX");
            flipped |= 42;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
