package vendor.huawei.hardware.radio_radar.V1_0;

import java.util.ArrayList;

public final class RilConstS32 {
    public static final int RIL_UNSOL_HW_RESET_CHR_IND = 2018;
    public static final int RIL_UNSOL_HW_RIL_CHR_IND = 2017;

    public static final String toString(int o) {
        if (o == 2017) {
            return "RIL_UNSOL_HW_RIL_CHR_IND";
        }
        if (o == 2018) {
            return "RIL_UNSOL_HW_RESET_CHR_IND";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 2017) == 2017) {
            list.add("RIL_UNSOL_HW_RIL_CHR_IND");
            flipped = 0 | 2017;
        }
        if ((o & 2018) == 2018) {
            list.add("RIL_UNSOL_HW_RESET_CHR_IND");
            flipped |= 2018;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
