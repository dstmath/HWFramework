package vendor.huawei.hardware.hisiradio.V1_0;

import java.util.ArrayList;

public final class CSGTYPE {
    public static final int CSG_ALLOW_LIST = 1;
    public static final int CSG_OPERATOR_LIST_ALLOW = 2;
    public static final int CSG_OPERATOR_LIST_FORBIDEN = 3;
    public static final int CSG_UNKNOW_LIST = 4;

    public static final String toString(int o) {
        if (o == 1) {
            return "CSG_ALLOW_LIST";
        }
        if (o == 2) {
            return "CSG_OPERATOR_LIST_ALLOW";
        }
        if (o == 3) {
            return "CSG_OPERATOR_LIST_FORBIDEN";
        }
        if (o == 4) {
            return "CSG_UNKNOW_LIST";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("CSG_ALLOW_LIST");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CSG_OPERATOR_LIST_ALLOW");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CSG_OPERATOR_LIST_FORBIDEN");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CSG_UNKNOW_LIST");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
