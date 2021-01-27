package vendor.huawei.hardware.tp.V1_0;

import java.util.ArrayList;

public final class ConstS32 {
    public static final int TP_CAP_TEST_RESULT_LEN = 100;
    public static final int TS_CHIP_INFO_LEN = 128;
    public static final int TS_GESTURE_DATA_LEN = 12;
    public static final int TS_ROI_DATA_LEN = 47;

    public static final String toString(int o) {
        if (o == 47) {
            return "TS_ROI_DATA_LEN";
        }
        if (o == 128) {
            return "TS_CHIP_INFO_LEN";
        }
        if (o == 12) {
            return "TS_GESTURE_DATA_LEN";
        }
        if (o == 100) {
            return "TP_CAP_TEST_RESULT_LEN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 47) == 47) {
            list.add("TS_ROI_DATA_LEN");
            flipped = 0 | 47;
        }
        if ((o & 128) == 128) {
            list.add("TS_CHIP_INFO_LEN");
            flipped |= 128;
        }
        if ((o & 12) == 12) {
            list.add("TS_GESTURE_DATA_LEN");
            flipped |= 12;
        }
        if ((o & 100) == 100) {
            list.add("TP_CAP_TEST_RESULT_LEN");
            flipped |= 100;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
