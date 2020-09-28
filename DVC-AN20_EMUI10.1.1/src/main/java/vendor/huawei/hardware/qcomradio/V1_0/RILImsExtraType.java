package vendor.huawei.hardware.qcomradio.V1_0;

import java.util.ArrayList;

public final class RILImsExtraType {
    public static final int IMS_EXTRA_TYPE_LTE_TO_IWLAN_HO_FAIL = 1;
    public static final int IMS_EXTRA_TYPE_NULL = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMS_EXTRA_TYPE_NULL";
        }
        if (o == 1) {
            return "IMS_EXTRA_TYPE_LTE_TO_IWLAN_HO_FAIL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("IMS_EXTRA_TYPE_NULL");
        if ((o & 1) == 1) {
            list.add("IMS_EXTRA_TYPE_LTE_TO_IWLAN_HO_FAIL");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
