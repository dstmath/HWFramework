package vendor.huawei.hardware.hisiradio.V1_0;

import java.util.ArrayList;

public final class RILImsStatusType {
    public static final int IMS_STATUS_TYPE_DISABLED = 0;
    public static final int IMS_STATUS_TYPE_ENABLED = 2;
    public static final int IMS_STATUS_TYPE_NOT_SUPPORTED = 3;
    public static final int IMS_STATUS_TYPE_PARTIALLY_ENABLED = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMS_STATUS_TYPE_DISABLED";
        }
        if (o == 1) {
            return "IMS_STATUS_TYPE_PARTIALLY_ENABLED";
        }
        if (o == 2) {
            return "IMS_STATUS_TYPE_ENABLED";
        }
        if (o == 3) {
            return "IMS_STATUS_TYPE_NOT_SUPPORTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("IMS_STATUS_TYPE_DISABLED");
        if ((o & 1) == 1) {
            list.add("IMS_STATUS_TYPE_PARTIALLY_ENABLED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("IMS_STATUS_TYPE_ENABLED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMS_STATUS_TYPE_NOT_SUPPORTED");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
