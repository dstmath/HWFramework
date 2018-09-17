package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class ImsConstS32 {
    public static final int IPV4_ADDR_LEN = 4;
    public static final int IPV6_ADDR_LEN = 16;
    public static final int MAX_ECONF_CALLED_NUM = 5;
    public static final int MAX_IMS_CALL_TYPE = 4;
    public static final int MAX_IMS_TECH_TYPE = 1;

    public static final String toString(int o) {
        if (o == 4) {
            return "IPV4_ADDR_LEN";
        }
        if (o == 16) {
            return "IPV6_ADDR_LEN";
        }
        if (o == 4) {
            return "MAX_IMS_CALL_TYPE";
        }
        if (o == 1) {
            return "MAX_IMS_TECH_TYPE";
        }
        if (o == 5) {
            return "MAX_ECONF_CALLED_NUM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 4) == 4) {
            list.add("IPV4_ADDR_LEN");
            flipped = 4;
        }
        if ((o & 16) == 16) {
            list.add("IPV6_ADDR_LEN");
            flipped |= 16;
        }
        if ((o & 4) == 4) {
            list.add("MAX_IMS_CALL_TYPE");
            flipped |= 4;
        }
        if ((o & 1) == 1) {
            list.add("MAX_IMS_TECH_TYPE");
            flipped |= 1;
        }
        if ((o & 5) == 5) {
            list.add("MAX_ECONF_CALLED_NUM");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
