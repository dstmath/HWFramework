package vendor.huawei.hardware.mtkradio.V1_0;

import java.util.ArrayList;

public final class CallInfoType {
    public static final int MT_CALL_DIAL_IMS_STK = 100;
    public static final int MT_CALL_GWSD = 10;
    public static final int MT_CALL_MISSED = 2;
    public static final int MT_CALL_NONE = 0;
    public static final int MT_CALL_NUMREDIRECT = 3;
    public static final int MT_CALL_REJECTED = 1;
    public static final int MT_CALL_RQ = 4;

    public static final String toString(int o) {
        if (o == 0) {
            return "MT_CALL_NONE";
        }
        if (o == 1) {
            return "MT_CALL_REJECTED";
        }
        if (o == 2) {
            return "MT_CALL_MISSED";
        }
        if (o == 3) {
            return "MT_CALL_NUMREDIRECT";
        }
        if (o == 4) {
            return "MT_CALL_RQ";
        }
        if (o == 10) {
            return "MT_CALL_GWSD";
        }
        if (o == 100) {
            return "MT_CALL_DIAL_IMS_STK";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("MT_CALL_NONE");
        if ((o & 1) == 1) {
            list.add("MT_CALL_REJECTED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MT_CALL_MISSED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("MT_CALL_NUMREDIRECT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("MT_CALL_RQ");
            flipped |= 4;
        }
        if ((o & 10) == 10) {
            list.add("MT_CALL_GWSD");
            flipped |= 10;
        }
        if ((o & 100) == 100) {
            list.add("MT_CALL_DIAL_IMS_STK");
            flipped |= 100;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
