package vendor.huawei.hardware.mtkradio.V1_0;

import java.util.ArrayList;

public final class DsbpState {
    public static final int DSBP_ENHANCEMENT_END = 0;
    public static final int DSBP_ENHANCEMENT_START = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "DSBP_ENHANCEMENT_END";
        }
        if (o == 1) {
            return "DSBP_ENHANCEMENT_START";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DSBP_ENHANCEMENT_END");
        if ((o & 1) == 1) {
            list.add("DSBP_ENHANCEMENT_START");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
