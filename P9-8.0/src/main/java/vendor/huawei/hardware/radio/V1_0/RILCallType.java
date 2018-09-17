package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RILCallType {
    public static final int RIL_CALL_TYPE_VOICE = 0;
    public static final int RIL_CALL_TYPE_VS_RX = 2;
    public static final int RIL_CALL_TYPE_VS_TX = 1;
    public static final int RIL_CALL_TYPE_VT = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "RIL_CALL_TYPE_VOICE";
        }
        if (o == 1) {
            return "RIL_CALL_TYPE_VS_TX";
        }
        if (o == 2) {
            return "RIL_CALL_TYPE_VS_RX";
        }
        if (o == 3) {
            return "RIL_CALL_TYPE_VT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 0) == 0) {
            list.add("RIL_CALL_TYPE_VOICE");
            flipped = 0;
        }
        if ((o & 1) == 1) {
            list.add("RIL_CALL_TYPE_VS_TX");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("RIL_CALL_TYPE_VS_RX");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("RIL_CALL_TYPE_VT");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
