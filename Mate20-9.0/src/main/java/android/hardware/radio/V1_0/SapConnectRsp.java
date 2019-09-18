package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SapConnectRsp {
    public static final int CONNECT_FAILURE = 1;
    public static final int CONNECT_OK_CALL_ONGOING = 4;
    public static final int MSG_SIZE_TOO_LARGE = 2;
    public static final int MSG_SIZE_TOO_SMALL = 3;
    public static final int SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "CONNECT_FAILURE";
        }
        if (o == 2) {
            return "MSG_SIZE_TOO_LARGE";
        }
        if (o == 3) {
            return "MSG_SIZE_TOO_SMALL";
        }
        if (o == 4) {
            return "CONNECT_OK_CALL_ONGOING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("CONNECT_FAILURE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MSG_SIZE_TOO_LARGE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("MSG_SIZE_TOO_SMALL");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CONNECT_OK_CALL_ONGOING");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
