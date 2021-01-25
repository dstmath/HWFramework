package vendor.huawei.hardware.tp.V1_0;

import java.util.ArrayList;

public final class TpCapTestResult {
    public static final int TP_INITIAL_FAIL = 4;
    public static final int TP_PANEL_REASON = 2;
    public static final int TP_SOFTWARE_REASON = 3;
    public static final int TP_TEST_FAIL = 1;
    public static final int TP_TEST_PASS = 0;
    public static final int TP_UNKNOWN_FAIL = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "TP_TEST_PASS";
        }
        if (o == 1) {
            return "TP_TEST_FAIL";
        }
        if (o == 2) {
            return "TP_PANEL_REASON";
        }
        if (o == 3) {
            return "TP_SOFTWARE_REASON";
        }
        if (o == 4) {
            return "TP_INITIAL_FAIL";
        }
        if (o == 5) {
            return "TP_UNKNOWN_FAIL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("TP_TEST_PASS");
        if ((o & 1) == 1) {
            list.add("TP_TEST_FAIL");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TP_PANEL_REASON");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TP_SOFTWARE_REASON");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TP_INITIAL_FAIL");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TP_UNKNOWN_FAIL");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
