package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RILImsCallState {
    public static final int RIL_IMS_CALL_ACTIVE = 0;
    public static final int RIL_IMS_CALL_ALERTING = 3;
    public static final int RIL_IMS_CALL_DIALING = 2;
    public static final int RIL_IMS_CALL_HOLDING = 1;
    public static final int RIL_IMS_CALL_INCOMING = 4;
    public static final int RIL_IMS_CALL_WAITING = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "RIL_IMS_CALL_ACTIVE";
        }
        if (o == 1) {
            return "RIL_IMS_CALL_HOLDING";
        }
        if (o == 2) {
            return "RIL_IMS_CALL_DIALING";
        }
        if (o == 3) {
            return "RIL_IMS_CALL_ALERTING";
        }
        if (o == 4) {
            return "RIL_IMS_CALL_INCOMING";
        }
        if (o == 5) {
            return "RIL_IMS_CALL_WAITING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("RIL_IMS_CALL_ACTIVE");
        if ((o & 1) == 1) {
            list.add("RIL_IMS_CALL_HOLDING");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("RIL_IMS_CALL_DIALING");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("RIL_IMS_CALL_ALERTING");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("RIL_IMS_CALL_INCOMING");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("RIL_IMS_CALL_WAITING");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
