package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class RttStatus {
    public static final int ABORTED = 8;
    public static final int FAILURE = 1;
    public static final int FAIL_AP_ON_DIFF_CHANNEL = 6;
    public static final int FAIL_BUSY_TRY_LATER = 12;
    public static final int FAIL_FTM_PARAM_OVERRIDE = 15;
    public static final int FAIL_INVALID_TS = 9;
    public static final int FAIL_NOT_SCHEDULED_YET = 4;
    public static final int FAIL_NO_CAPABILITY = 7;
    public static final int FAIL_NO_RSP = 2;
    public static final int FAIL_PROTOCOL = 10;
    public static final int FAIL_REJECTED = 3;
    public static final int FAIL_SCHEDULE = 11;
    public static final int FAIL_TM_TIMEOUT = 5;
    public static final int INVALID_REQ = 13;
    public static final int NO_WIFI = 14;
    public static final int SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "FAILURE";
        }
        if (o == 2) {
            return "FAIL_NO_RSP";
        }
        if (o == 3) {
            return "FAIL_REJECTED";
        }
        if (o == 4) {
            return "FAIL_NOT_SCHEDULED_YET";
        }
        if (o == 5) {
            return "FAIL_TM_TIMEOUT";
        }
        if (o == 6) {
            return "FAIL_AP_ON_DIFF_CHANNEL";
        }
        if (o == 7) {
            return "FAIL_NO_CAPABILITY";
        }
        if (o == 8) {
            return "ABORTED";
        }
        if (o == 9) {
            return "FAIL_INVALID_TS";
        }
        if (o == 10) {
            return "FAIL_PROTOCOL";
        }
        if (o == 11) {
            return "FAIL_SCHEDULE";
        }
        if (o == 12) {
            return "FAIL_BUSY_TRY_LATER";
        }
        if (o == 13) {
            return "INVALID_REQ";
        }
        if (o == 14) {
            return "NO_WIFI";
        }
        if (o == 15) {
            return "FAIL_FTM_PARAM_OVERRIDE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("FAILURE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FAIL_NO_RSP");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("FAIL_REJECTED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("FAIL_NOT_SCHEDULED_YET");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("FAIL_TM_TIMEOUT");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("FAIL_AP_ON_DIFF_CHANNEL");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("FAIL_NO_CAPABILITY");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("ABORTED");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("FAIL_INVALID_TS");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("FAIL_PROTOCOL");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("FAIL_SCHEDULE");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("FAIL_BUSY_TRY_LATER");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("INVALID_REQ");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("NO_WIFI");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("FAIL_FTM_PARAM_OVERRIDE");
            flipped |= 15;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
