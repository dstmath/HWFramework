package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RILImsHandoverState {
    public static final int IMS_HANDOVER_STATE_CANCEL = 3;
    public static final int IMS_HANDOVER_STATE_COMPLETE_FAIL = 2;
    public static final int IMS_HANDOVER_STATE_COMPLETE_SUCCESS = 1;
    public static final int IMS_HANDOVER_STATE_NOT_TRIGGERED = 4;
    public static final int IMS_HANDOVER_STATE_START = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMS_HANDOVER_STATE_START";
        }
        if (o == 1) {
            return "IMS_HANDOVER_STATE_COMPLETE_SUCCESS";
        }
        if (o == 2) {
            return "IMS_HANDOVER_STATE_COMPLETE_FAIL";
        }
        if (o == 3) {
            return "IMS_HANDOVER_STATE_CANCEL";
        }
        if (o == 4) {
            return "IMS_HANDOVER_STATE_NOT_TRIGGERED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 0) == 0) {
            list.add("IMS_HANDOVER_STATE_START");
            flipped = 0;
        }
        if ((o & 1) == 1) {
            list.add("IMS_HANDOVER_STATE_COMPLETE_SUCCESS");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("IMS_HANDOVER_STATE_COMPLETE_FAIL");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMS_HANDOVER_STATE_CANCEL");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IMS_HANDOVER_STATE_NOT_TRIGGERED");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
