package vendor.huawei.hardware.weaver.V1_0;

import java.util.ArrayList;

public final class WeaverErrStatus {
    public static final int STATUS_HARDWARE_ERR = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OTHER_ERR = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "STATUS_OK";
        }
        if (o == 1) {
            return "STATUS_HARDWARE_ERR";
        }
        if (o == 2) {
            return "STATUS_OTHER_ERR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("STATUS_OK");
        if ((o & 1) == 1) {
            list.add("STATUS_HARDWARE_ERR");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("STATUS_OTHER_ERR");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
