package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class HighBitsALModeID {
    public static final int MODE_LRE = 512;
    public static final int MODE_LRE_DISABLE = 1024;
    public static final int MODE_SRE = 256;
    public static final int MODE_SRE_DISABLE = 768;

    public static final String toString(int o) {
        if (o == 256) {
            return "MODE_SRE";
        }
        if (o == 512) {
            return "MODE_LRE";
        }
        if (o == 768) {
            return "MODE_SRE_DISABLE";
        }
        if (o == 1024) {
            return "MODE_LRE_DISABLE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 256) == 256) {
            list.add("MODE_SRE");
            flipped = 0 | 256;
        }
        if ((o & 512) == 512) {
            list.add("MODE_LRE");
            flipped |= 512;
        }
        if ((o & MODE_SRE_DISABLE) == 768) {
            list.add("MODE_SRE_DISABLE");
            flipped |= MODE_SRE_DISABLE;
        }
        if ((o & 1024) == 1024) {
            list.add("MODE_LRE_DISABLE");
            flipped |= 1024;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
