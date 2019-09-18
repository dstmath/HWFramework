package vendor.huawei.hardware.tp.V1_0;

import java.util.ArrayList;

public final class RunningTestStatus {
    public static final int RUNNING_TEST_LAST_TIME = 2;
    public static final int RUNNING_TEST_LCD_TEST = 4;
    public static final int RUNNING_TEST_OTHER_TEST = 1;
    public static final int RUNNING_TEST_UNKNOWN = 255;
    public static final int RUNNING_TEST_VIDEO_TEST = 8;

    public static final String toString(int o) {
        if (o == 1) {
            return "RUNNING_TEST_OTHER_TEST";
        }
        if (o == 2) {
            return "RUNNING_TEST_LAST_TIME";
        }
        if (o == 4) {
            return "RUNNING_TEST_LCD_TEST";
        }
        if (o == 8) {
            return "RUNNING_TEST_VIDEO_TEST";
        }
        if (o == 255) {
            return "RUNNING_TEST_UNKNOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("RUNNING_TEST_OTHER_TEST");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("RUNNING_TEST_LAST_TIME");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("RUNNING_TEST_LCD_TEST");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("RUNNING_TEST_VIDEO_TEST");
            flipped |= 8;
        }
        if ((o & 255) == 255) {
            list.add("RUNNING_TEST_UNKNOWN");
            flipped |= 255;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
