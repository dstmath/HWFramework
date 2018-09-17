package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RILImsSrvType {
    public static final int IMS_SRV_TYPE_SMS = 1;
    public static final int IMS_SRV_TYPE_VOIP = 2;
    public static final int IMS_SRV_TYPE_VT = 3;

    public static final String toString(int o) {
        if (o == 1) {
            return "IMS_SRV_TYPE_SMS";
        }
        if (o == 2) {
            return "IMS_SRV_TYPE_VOIP";
        }
        if (o == 3) {
            return "IMS_SRV_TYPE_VT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("IMS_SRV_TYPE_SMS");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMS_SRV_TYPE_VOIP");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMS_SRV_TYPE_VT");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
