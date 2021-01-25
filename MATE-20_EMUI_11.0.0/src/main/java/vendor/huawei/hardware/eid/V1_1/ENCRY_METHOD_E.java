package vendor.huawei.hardware.eid.V1_1;

import java.util.ArrayList;

public final class ENCRY_METHOD_E {
    public static final int ENCRY_GET_ORI_IMG = 6;
    public static final int ENCRY_GET_SEC_IMG = 5;
    public static final int ENCRY_GET_SM4_DATA = 4;
    public static final int ENCRY_SET_BACKCAMERA = 7;
    public static final int ENCRY_SET_SECMODE = 3;
    public static final int ENCRY_SM4_LIVE = 1;
    public static final int ENCRY_UNION_LIVE = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "ENCRY_SM4_LIVE";
        }
        if (o == 2) {
            return "ENCRY_UNION_LIVE";
        }
        if (o == 3) {
            return "ENCRY_SET_SECMODE";
        }
        if (o == 4) {
            return "ENCRY_GET_SM4_DATA";
        }
        if (o == 5) {
            return "ENCRY_GET_SEC_IMG";
        }
        if (o == 6) {
            return "ENCRY_GET_ORI_IMG";
        }
        if (o == 7) {
            return "ENCRY_SET_BACKCAMERA";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("ENCRY_SM4_LIVE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ENCRY_UNION_LIVE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("ENCRY_SET_SECMODE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ENCRY_GET_SM4_DATA");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ENCRY_GET_SEC_IMG");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("ENCRY_GET_ORI_IMG");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("ENCRY_SET_BACKCAMERA");
            flipped |= 7;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
