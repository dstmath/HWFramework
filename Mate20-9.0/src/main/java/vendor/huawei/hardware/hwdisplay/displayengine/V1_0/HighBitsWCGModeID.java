package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class HighBitsWCGModeID {
    public static final int MODE_ADOBERGB = 4096;
    public static final int MODE_DISPLAYP3 = 8192;
    public static final int MODE_SUPERGAMUT = 12288;

    public static final String toString(int o) {
        if (o == 4096) {
            return "MODE_ADOBERGB";
        }
        if (o == 8192) {
            return "MODE_DISPLAYP3";
        }
        if (o == 12288) {
            return "MODE_SUPERGAMUT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 4096) == 4096) {
            list.add("MODE_ADOBERGB");
            flipped = 0 | 4096;
        }
        if ((o & 8192) == 8192) {
            list.add("MODE_DISPLAYP3");
            flipped |= 8192;
        }
        if ((o & MODE_SUPERGAMUT) == 12288) {
            list.add("MODE_SUPERGAMUT");
            flipped |= MODE_SUPERGAMUT;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
