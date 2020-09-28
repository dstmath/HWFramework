package vendor.huawei.hardware.qcomradio.V1_0;

import java.util.ArrayList;

public final class RILImsCallType {
    public static final int CALL_TYPE_CS_VS_RX = 6;
    public static final int CALL_TYPE_CS_VS_TX = 5;
    public static final int CALL_TYPE_PS_VS_RX = 8;
    public static final int CALL_TYPE_PS_VS_TX = 7;
    public static final int CALL_TYPE_SMS = 10;
    public static final int CALL_TYPE_UNKNOWN = 9;
    public static final int CALL_TYPE_UT = 11;
    public static final int CALL_TYPE_VOICE = 0;
    public static final int CALL_TYPE_VT = 3;
    public static final int CALL_TYPE_VT_NODIR = 4;
    public static final int CALL_TYPE_VT_RX = 2;
    public static final int CALL_TYPE_VT_TX = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "CALL_TYPE_VOICE";
        }
        if (o == 1) {
            return "CALL_TYPE_VT_TX";
        }
        if (o == 2) {
            return "CALL_TYPE_VT_RX";
        }
        if (o == 3) {
            return "CALL_TYPE_VT";
        }
        if (o == 4) {
            return "CALL_TYPE_VT_NODIR";
        }
        if (o == 5) {
            return "CALL_TYPE_CS_VS_TX";
        }
        if (o == 6) {
            return "CALL_TYPE_CS_VS_RX";
        }
        if (o == 7) {
            return "CALL_TYPE_PS_VS_TX";
        }
        if (o == 8) {
            return "CALL_TYPE_PS_VS_RX";
        }
        if (o == 9) {
            return "CALL_TYPE_UNKNOWN";
        }
        if (o == 10) {
            return "CALL_TYPE_SMS";
        }
        if (o == 11) {
            return "CALL_TYPE_UT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("CALL_TYPE_VOICE");
        if ((o & 1) == 1) {
            list.add("CALL_TYPE_VT_TX");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CALL_TYPE_VT_RX");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CALL_TYPE_VT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CALL_TYPE_VT_NODIR");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("CALL_TYPE_CS_VS_TX");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("CALL_TYPE_CS_VS_RX");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("CALL_TYPE_PS_VS_TX");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("CALL_TYPE_PS_VS_RX");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("CALL_TYPE_UNKNOWN");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("CALL_TYPE_SMS");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("CALL_TYPE_UT");
            flipped |= 11;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
