package vendor.huawei.hardware.dolby.dms.V1_0;

import java.util.ArrayList;

public final class DolbyClientType {
    public static final int DOLBY_CLIENT_AC4DEC = 5;
    public static final int DOLBY_CLIENT_DAP_EFF = 1;
    public static final int DOLBY_CLIENT_DP_EFF = 2;
    public static final int DOLBY_CLIENT_INVALID = -1;
    public static final int DOLBY_CLIENT_LAST = 6;
    public static final int DOLBY_CLIENT_NATIVE_SERVICE = 3;
    public static final int DOLBY_CLIENT_UDC = 0;
    public static final int DOLBY_CLIENT_UDC_JOC = 4;

    public static final String toString(int o) {
        if (o == -1) {
            return "DOLBY_CLIENT_INVALID";
        }
        if (o == 0) {
            return "DOLBY_CLIENT_UDC";
        }
        if (o == 1) {
            return "DOLBY_CLIENT_DAP_EFF";
        }
        if (o == 2) {
            return "DOLBY_CLIENT_DP_EFF";
        }
        if (o == 3) {
            return "DOLBY_CLIENT_NATIVE_SERVICE";
        }
        if (o == 4) {
            return "DOLBY_CLIENT_UDC_JOC";
        }
        if (o == 5) {
            return "DOLBY_CLIENT_AC4DEC";
        }
        if (o == 6) {
            return "DOLBY_CLIENT_LAST";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & -1) == -1) {
            list.add("DOLBY_CLIENT_INVALID");
            flipped = 0 | -1;
        }
        list.add("DOLBY_CLIENT_UDC");
        if ((o & 1) == 1) {
            list.add("DOLBY_CLIENT_DAP_EFF");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("DOLBY_CLIENT_DP_EFF");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("DOLBY_CLIENT_NATIVE_SERVICE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("DOLBY_CLIENT_UDC_JOC");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("DOLBY_CLIENT_AC4DEC");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("DOLBY_CLIENT_LAST");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
