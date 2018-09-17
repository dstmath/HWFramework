package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class HighBitsCompModeID {
    public static final int MODE_COLOR_ENHANCE = 1048576;
    public static final int MODE_EYE_PROTECT = 2097152;

    public static final String toString(int o) {
        if (o == MODE_COLOR_ENHANCE) {
            return "MODE_COLOR_ENHANCE";
        }
        if (o == MODE_EYE_PROTECT) {
            return "MODE_EYE_PROTECT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & MODE_COLOR_ENHANCE) == MODE_COLOR_ENHANCE) {
            list.add("MODE_COLOR_ENHANCE");
            flipped = MODE_COLOR_ENHANCE;
        }
        if ((o & MODE_EYE_PROTECT) == MODE_EYE_PROTECT) {
            list.add("MODE_EYE_PROTECT");
            flipped |= MODE_EYE_PROTECT;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
