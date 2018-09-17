package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RILUICCAUTHRESPSTATUSTYPEENUM {
    public static final int AUTH_RESP_FAIL = 1;
    public static final int AUTH_RESP_SUCCESS = 0;
    public static final int AUTH_RESP_SYNC_FAIL = 2;
    public static final int AUTH_RESP_UNSUPPORTED = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "AUTH_RESP_SUCCESS";
        }
        if (o == 1) {
            return "AUTH_RESP_FAIL";
        }
        if (o == 2) {
            return "AUTH_RESP_SYNC_FAIL";
        }
        if (o == 3) {
            return "AUTH_RESP_UNSUPPORTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 0) == 0) {
            list.add("AUTH_RESP_SUCCESS");
            flipped = 0;
        }
        if ((o & 1) == 1) {
            list.add("AUTH_RESP_FAIL");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("AUTH_RESP_SYNC_FAIL");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("AUTH_RESP_UNSUPPORTED");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
