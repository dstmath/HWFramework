package vendor.huawei.hardware.tp.V1_0;

import java.util.ArrayList;

public final class TpSnrTestResult {
    public static final int TP_SNR_TEST_FAIL_PANEL = 2;
    public static final int TP_SNR_TEST_FAIL_SOFTWARE = 1;
    public static final int TP_SNR_TEST_FAIL_UNKNOWN = 3;
    public static final int TP_SNR_TEST_PASS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "TP_SNR_TEST_PASS";
        }
        if (o == 1) {
            return "TP_SNR_TEST_FAIL_SOFTWARE";
        }
        if (o == 2) {
            return "TP_SNR_TEST_FAIL_PANEL";
        }
        if (o == 3) {
            return "TP_SNR_TEST_FAIL_UNKNOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("TP_SNR_TEST_PASS");
        if ((o & 1) == 1) {
            list.add("TP_SNR_TEST_FAIL_SOFTWARE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TP_SNR_TEST_FAIL_PANEL");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TP_SNR_TEST_FAIL_UNKNOWN");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
