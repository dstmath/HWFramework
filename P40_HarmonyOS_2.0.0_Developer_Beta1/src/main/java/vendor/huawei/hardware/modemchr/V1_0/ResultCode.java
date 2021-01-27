package vendor.huawei.hardware.modemchr.V1_0;

import java.util.ArrayList;

public final class ResultCode {
    public static final int GENERAL_ERROR = -1;
    public static final int NOT_SUPPORTED = -2;
    public static final int RESULT_OK = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "RESULT_OK";
        }
        if (o == -1) {
            return "GENERAL_ERROR";
        }
        if (o == -2) {
            return "NOT_SUPPORTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("RESULT_OK");
        if ((o & -1) == -1) {
            list.add("GENERAL_ERROR");
            flipped = 0 | -1;
        }
        if ((o & -2) == -2) {
            list.add("NOT_SUPPORTED");
            flipped |= -2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
