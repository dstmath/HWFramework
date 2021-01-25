package vendor.huawei.hardware.mtkradio.V1_0;

import java.util.ArrayList;

public final class ImsRegStatusReportType {
    public static final int IMS_REGISTERED = 1;
    public static final int IMS_REGISTERING = 0;
    public static final int IMS_REGISTER_FAIL = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMS_REGISTERING";
        }
        if (o == 1) {
            return "IMS_REGISTERED";
        }
        if (o == 2) {
            return "IMS_REGISTER_FAIL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("IMS_REGISTERING");
        if ((o & 1) == 1) {
            list.add("IMS_REGISTERED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("IMS_REGISTER_FAIL");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
